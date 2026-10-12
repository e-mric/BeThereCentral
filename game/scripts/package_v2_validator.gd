extends RefCounted
class_name CommunityPackageV2Validator

const RoomPhysics = preload("res://scripts/room_physics.gd")

const MAX_PACKAGE_BYTES := 65_536
const ROOT_KEYS := ["packageVersion", "packageId", "contentRevision", "features", "assets", "attribution", "room", "characters", "quest", "gameplay"]
const ROOT_ASSETS_KEYS := ["catalogId", "catalogRevision"]
const ROOM_KEYS := ["schemaVersion", "companyName", "room", "objects"]
const GEOMETRY_KEYS := ["id", "width", "height", "polygon"]
const OBJECT_KEYS := ["id", "kind", "x", "y", "width", "height"]
const CHARACTER_KEYS := ["id", "displayName", "introduction", "appearance"]
const APPEARANCE_KEYS := ["bodyPalette", "hair", "hairPalette", "top", "topPalette", "trousersPalette", "accessory"]
const QUEST_KEYS := ["id", "template", "title", "learningObjective", "introduction", "clues", "question", "answers", "correctAnswerId", "hint", "explanation", "reward"]
const CLUE_KEYS := ["id", "title", "text"]
const ANSWER_KEYS := ["id", "text"]
const REWARD_KEYS := ["id", "label"]
const GAMEPLAY_KEYS := ["playerCharacterId", "spawn", "npc", "clues"]
const POSITION_KEYS := ["x", "y"]
const NPC_KEYS := ["characterId", "x", "y"]
const GAMEPLAY_CLUE_KEYS := ["clueId", "x", "y"]
const POLYGON := [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]]
const PACKAGE_FEATURES := ["rooms-v2"]
const PLAY_FEATURES := ["rooms-v2", "play-learn-v1"]
const ASSET_PACKAGE := {"catalogId": "betherecentral-community", "catalogRevision": 2}
const ATTRIBUTION := ["betherecentral-original-community-assets"]

static func parse_file(path: String, catalog: Dictionary) -> Dictionary:
	var file := FileAccess.open(path, FileAccess.READ)
	if file == null:
		return _failure("Could not open room file.")
	if file.get_length() > MAX_PACKAGE_BYTES:
		file.close()
		return _failure("Room file is larger than 65,536 bytes.")
	var source := file.get_as_text()
	file.close()
	if source.to_utf8_buffer().size() > MAX_PACKAGE_BYTES:
		return _failure("Room file is larger than 65,536 bytes.")
	var json := JSON.new()
	if json.parse(source) != OK:
		return _failure("This file is not valid room JSON.")
	return validate(json.data, catalog)

static func validate(value: Variant, catalog: Dictionary) -> Dictionary:
	if not _exact_keys(value, ROOT_KEYS):
		return _failure("This file does not have the supported room format.")
	if value.packageVersion != 2 or value.packageId != "betherecentral.sample-room" or not _safe_revision(value.contentRevision):
		return _failure("This room version is not supported.")
	var play_enabled: bool = value.features == PLAY_FEATURES
	if not play_enabled and value.features != PACKAGE_FEATURES:
		return _failure("This room uses unsupported features.")
	if not _exact_keys(value.assets, ROOT_ASSETS_KEYS) or value.assets.catalogId != ASSET_PACKAGE.catalogId or value.assets.catalogRevision != ASSET_PACKAGE.catalogRevision:
		return _failure("This room uses an unsupported asset collection.")
	if value.attribution != ATTRIBUTION:
		return _failure("Room attribution is missing or changed.")
	if not _valid_catalog(catalog):
		return _failure("The trusted room asset collection is unavailable.")
	var room_result := _validate_room(value.room, catalog.props)
	if not room_result.ok:
		return room_result
	if not play_enabled:
		if not value.characters is Array or not value.characters.is_empty() or value.quest != null or value.gameplay != null:
			return _failure("A room-only file cannot include play content.")
		return {"ok": true, "value": value.duplicate(true), "play_enabled": false, "error": ""}
	var play_result := _validate_play_content(value.characters, value.quest, value.gameplay, room_result.objects, catalog)
	if not play_result.ok:
		return play_result
	return {"ok": true, "value": value.duplicate(true), "play_enabled": true, "error": ""}

static func _validate_room(room: Variant, props: Array) -> Dictionary:
	if not _exact_keys(room, ROOM_KEYS):
		return _failure("The room must contain the supported room fields only.")
	if room.schemaVersion != 2 or not _valid_text(room.companyName, 1, 60):
		return _failure("The room version or room label is invalid.")
	if not _same_geometry(room.room):
		return _failure("Room shape must match the trusted sample room.")
	if not room.objects is Array or room.objects.size() > 24:
		return _failure("A room can contain at most 24 props.")
	var by_id := {}
	var bookcase_count := 0
	for item in room.objects:
		if not _exact_keys(item, OBJECT_KEYS):
			return _failure("Each room prop must use the supported fields only.")
		if not _safe_id(item.id) or by_id.has(item.id):
			return _failure("Room prop IDs must be unique safe names.")
		var prop: Dictionary = _prop_by_id(props, item.kind)
		if prop.is_empty():
			return _failure("A room prop is not in the trusted collection.")
		if not _safe_integer(item.x, 0, 320) or not _safe_integer(item.y, 0, 224):
			return _failure("Room prop placement must use whole room coordinates.")
		if not _safe_integer(item.width, prop.minSize[0], prop.maxSize[0]) or not _safe_integer(item.height, prop.minSize[1], prop.maxSize[1]):
			return _failure("A room prop is outside its supported size.")
		if not _box_inside_polygon(Rect2(item.x, item.y, item.width, item.height), room.room.polygon):
			return _failure("A room prop must fit inside the room outline.")
		by_id[item.id] = item
		if item.kind == "secret-bookshelf":
			bookcase_count += 1
	if bookcase_count > 1:
		return _failure("A room can contain only one secret bookshelf.")
	return {"ok": true, "objects": room.objects, "by_id": by_id, "error": ""}

static func _validate_play_content(characters: Variant, quest: Variant, gameplay: Variant, objects: Array, catalog: Dictionary) -> Dictionary:
	if not characters is Array or characters.size() != 2:
		return _failure("A learning room needs two characters.")
	var char_ids := {}
	for character in characters:
		if not _exact_keys(character, CHARACTER_KEYS) or not _safe_id(character.id) or char_ids.has(character.id):
			return _failure("Character IDs and fields are invalid.")
		if not _valid_text(character.displayName, 1, 40) or not _valid_text(character.introduction, 0, 160):
			return _failure("A character name or introduction is outside its supported length.")
		if not _valid_appearance(character.appearance, catalog):
			return _failure("A character appearance uses an unsupported choice.")
		char_ids[character.id] = true
	if not _exact_keys(quest, QUEST_KEYS) or not _safe_id(quest.id) or quest.template != "inspect-two-answer-one-v1":
		return _failure("This learning activity is not supported.")
	for spec in [[quest.title, 1, 80], [quest.learningObjective, 1, 160], [quest.introduction, 1, 600], [quest.question, 1, 320], [quest.hint, 1, 800], [quest.explanation, 1, 800]]:
		if not _valid_text(spec[0], spec[1], spec[2]):
			return _failure("A learning activity text field is outside its supported length.")
	if not quest.clues is Array or quest.clues.size() != 2 or not quest.answers is Array or quest.answers.size() != 3:
		return _failure("The learning activity needs two clues and three answers.")
	var clue_ids := {}
	for clue in quest.clues:
		if not _exact_keys(clue, CLUE_KEYS) or not _safe_id(clue.id) or clue_ids.has(clue.id) or not _valid_text(clue.title, 1, 60) or not _valid_text(clue.text, 1, 600):
			return _failure("A learning clue is invalid.")
		clue_ids[clue.id] = true
	var answer_ids := {}
	for answer in quest.answers:
		if not _exact_keys(answer, ANSWER_KEYS) or not _safe_id(answer.id) or answer_ids.has(answer.id) or not _valid_text(answer.text, 1, 160):
			return _failure("A learning answer is invalid.")
		answer_ids[answer.id] = true
	if not answer_ids.has(quest.correctAnswerId) or not _exact_keys(quest.reward, REWARD_KEYS) or not _safe_id(quest.reward.id) or not _valid_text(quest.reward.label, 1, 80):
		return _failure("The answer or reward reference is invalid.")
	if not _exact_keys(gameplay, GAMEPLAY_KEYS) or not char_ids.has(gameplay.playerCharacterId):
		return _failure("The learning room's character references are invalid.")
	if not _exact_keys(gameplay.spawn, POSITION_KEYS) or not _exact_keys(gameplay.npc, NPC_KEYS) or not char_ids.has(gameplay.npc.characterId) or gameplay.npc.characterId == gameplay.playerCharacterId:
		return _failure("The learning room's starting positions are invalid.")
	if not gameplay.clues is Array or gameplay.clues.size() != 2:
		return _failure("The learning room needs two clue locations.")
	var points := [gameplay.spawn, {"x": gameplay.npc.x, "y": gameplay.npc.y}]
	var placed_clues := {}
	for clue_location in gameplay.clues:
		if not _exact_keys(clue_location, GAMEPLAY_CLUE_KEYS) or not clue_ids.has(clue_location.clueId) or placed_clues.has(clue_location.clueId):
			return _failure("A clue location reference is invalid.")
		placed_clues[clue_location.clueId] = true
		points.append({"x": clue_location.x, "y": clue_location.y})
	if placed_clues.size() != clue_ids.size():
		return _failure("Every clue needs one room location.")
	for point in points:
		if not _valid_gameplay_point(point, objects, catalog.props):
			return _failure("A gameplay location is blocked or outside the room.")
	if not _reachable_points(points, gameplay.spawn, objects, catalog.props):
		return _failure("The starting point cannot reach every learning interaction.")
	return {"ok": true, "error": ""}

static func _valid_gameplay_point(point: Dictionary, objects: Array, props: Array) -> bool:
	if not _exact_keys(point, POSITION_KEYS) or not _safe_integer(point.x, 0, 320) or not _safe_integer(point.y, 0, 224) or posmod(int(point.x), 4) != 0 or posmod(int(point.y), 4) != 0:
		return false
	var polygon := [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]]
	return RoomPhysics.can_occupy(Vector2(point.x, point.y), polygon, objects, props)

static func _reachable_points(points: Array, spawn: Dictionary, objects: Array, props: Array) -> bool:
	var start := Vector2i(spawn.x, spawn.y)
	var seen := {start: true}
	var queue: Array[Vector2i] = [start]
	var cursor := 0
	var targets := {}
	for point in points:
		targets[Vector2i(point.x, point.y)] = true
	while cursor < queue.size():
		var current := queue[cursor]
		cursor += 1
		for delta in [Vector2i(0, -4), Vector2i(4, 0), Vector2i(0, 4), Vector2i(-4, 0)]:
			var next: Vector2i = current + delta
			if seen.has(next) or not RoomPhysics.can_occupy(Vector2(next), POLYGON, objects, props):
				continue
			seen[next] = true
			queue.append(next)
	for target in targets.keys():
		if not seen.has(target):
			return false
	for item in objects:
		var prop: Dictionary = _prop_by_id(props, item.kind)
		if prop.get("interaction") == null:
			continue
		if prop.get("interaction") == "white-construct":
			var approach := Vector2i(int(floor((item.x + item.width * 0.5) / 4.0 + 0.5)) * 4, int(ceil((item.y + item.height + 8) / 4.0)) * 4)
			if not seen.has(approach) or not RoomPhysics.can_occupy(Vector2(approach), POLYGON, objects, props):
				return false
		else:
			var rect := Rect2(item.x, item.y, item.width, item.height)
			var approach_found := false
			for cell in seen.keys():
				var closest := Vector2(clampf(cell.x, rect.position.x, rect.end.x), clampf(cell.y, rect.position.y, rect.end.y))
				if Vector2(cell).distance_to(closest) <= 24.0:
					approach_found = true
					break
			if not approach_found:
				return false
	return true

static func _valid_appearance(value: Variant, catalog: Dictionary) -> bool:
	if _exact_keys(value, ["presetId"]):
		var presets: Variant = catalog.get("avatarPresets", {})
		return presets is Dictionary and presets.has(value.presetId)
	if not _exact_keys(value, APPEARANCE_KEYS):
		return false
	var schema: Dictionary = catalog.character
	for key in APPEARANCE_KEYS:
		if not schema.has(key) or not schema[key] is Array or not schema[key].has(value[key]):
			return false
	return true

static func _valid_catalog(value: Dictionary) -> bool:
	if value.get("catalogId") != "betherecentral-community" or value.get("catalogRevision") != 2 or value.get("attributionId") != "betherecentral-original-community-assets":
		return false
	if not value.get("props") is Array or not value.get("character") is Dictionary:
		return false
	for prop in value.props:
		if not prop is Dictionary or not _safe_id(prop.get("id")) or not prop.get("image", "").begins_with("v1/") or prop.image.contains("..") or prop.image.contains("\\"):
			return false
	return true

static func _prop_by_id(props: Array, prop_id: Variant) -> Dictionary:
	if not prop_id is String:
		return {}
	for prop in props:
		if prop.id == prop_id:
			return prop
	return {}

static func _same_geometry(room: Variant) -> bool:
	if not _exact_keys(room, GEOMETRY_KEYS) or room.id != "studio-demo" or room.width != 320 or room.height != 224 or not room.polygon is Array or room.polygon.size() != POLYGON.size():
		return false
	for i in range(POLYGON.size()):
		if not room.polygon[i] is Array or room.polygon[i].size() != 2:
			return false
		for axis in range(2):
			if not _safe_integer(room.polygon[i][axis], 0, 320) or room.polygon[i][axis] != POLYGON[i][axis]:
				return false
	return true

static func _box_inside_polygon(box: Rect2, polygon: Array) -> bool:
	for point in [box.position, Vector2(box.end.x, box.position.y), Vector2(box.position.x, box.end.y), box.end]:
		if not _inside(point, polygon):
			return false
	return true

static func _inside(point: Vector2, polygon: Array) -> bool:
	var points := PackedVector2Array()
	for vertex in polygon:
		points.append(Vector2(vertex[0], vertex[1]))
	return Geometry2D.is_point_in_polygon(point, points)

static func _exact_keys(value: Variant, expected: Array) -> bool:
	if not value is Dictionary or value.size() != expected.size():
		return false
	for key in expected:
		if not value.has(key):
			return false
	return true

static func _valid_text(value: Variant, minimum: int, maximum: int) -> bool:
	if not value is String:
		return false
	var units := 0
	for i in range(value.length()):
		var point: int = value.unicode_at(i)
		if point <= 31 or (point >= 127 and point <= 159):
			return false
		units += 2 if point > 0xFFFF else 1
	return units >= minimum and units <= maximum and value.strip_edges() == value

static func _safe_id(value: Variant) -> bool:
	if not value is String or value.is_empty() or value.length() > 40:
		return false
	for i in range(value.length()):
		var code: int = value.unicode_at(i)
		if not ((code >= 48 and code <= 57) or (code >= 65 and code <= 90) or (code >= 97 and code <= 122) or code == 45 or code == 95):
			return false
	return true

static func _safe_revision(value: Variant) -> bool:
	return _safe_integer(value, 1, 9007199254740991)

static func _safe_integer(value: Variant, minimum: int, maximum: int) -> bool:
	return (value is int or value is float) and is_finite(float(value)) and floorf(float(value)) == float(value) and value >= minimum and value <= maximum

static func _failure(message: String) -> Dictionary:
	return {"ok": false, "value": null, "error": message}
