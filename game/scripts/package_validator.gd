extends RefCounted
class_name CommunityPackageValidator

const MAX_PACKAGE_BYTES := 65_536
const ROOT_KEYS := ["packageVersion", "packageId", "contentRevision", "features", "assets", "attribution", "room"]
const ASSET_KEYS := ["id", "type", "path", "mimeType", "license", "attribution"]
const ATTRIBUTION_KEYS := ["id", "text"]
const SCENE_KEYS := ["schemaVersion", "companyName", "room", "objects"]
const ROOM_KEYS := ["id", "width", "height", "polygon"]
const OBJECT_KEYS := ["id", "kind", "x", "y", "width", "height"]
const POLYGON := [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]]
const KINDS := ["desk", "plant", "sofa", "rug"]
const CATALOG := [{
	"id": "campus-props-atlas", "type": "image-atlas",
	"path": "composeApp/src/commonMain/composeResources/drawable/campus_props.png",
	"mimeType": "image/png", "license": "MIT",
	"attribution": "BeThereCentral project; original generated pixel-art atlas. See docs/design/generation-prompts.md and docs/assets/PIXEL_ART.md."
}]
const ATTRIBUTIONS := [{
	"id": "betherecentral-sample-room",
	"text": "Fictional BeThereCentral Room Studio sample. Room layout and company label are illustrative and are not mapped to a real tenant room."
}]

static func parse_file(path: String) -> Dictionary:
	var file := FileAccess.open(path, FileAccess.READ)
	if file == null:
		return _failure("Could not open package file.")
	if file.get_length() > MAX_PACKAGE_BYTES:
		file.close()
		return _failure("Package must be at most 65,536 UTF-8 bytes.")
	var source := file.get_as_text()
	file.close()
	if source.to_utf8_buffer().size() > MAX_PACKAGE_BYTES:
		return _failure("Package must be at most 65,536 UTF-8 bytes.")
	var json := JSON.new()
	if json.parse(source) != OK:
		return _failure("Package must be valid JSON.")
	return validate(json.data)

static func validate(value: Variant) -> Dictionary:
	if not _exact_keys(value, ROOT_KEYS):
		return _failure("Package must contain exactly the documented v1 fields.")
	if value.packageVersion != 1 or value.packageId != "betherecentral.sample-room" or not _is_safe_revision(value.contentRevision):
		return _failure("Unsupported package version, ID, or content revision.")
	if value.features != ["rooms-v1"]:
		return _failure("Unsupported or missing required package feature.")
	if not _same_catalog(value.assets):
		return _failure("Asset catalog must match the trusted bundled asset catalog.")
	if not _same_attribution(value.attribution):
		return _failure("Package attribution is missing or changed.")
	var scene_result := _validate_scene(value.room)
	if not scene_result.ok:
		return scene_result
	return {"ok": true, "value": value.duplicate(true), "error": ""}

static func _validate_scene(scene: Variant) -> Dictionary:
	if not _exact_keys(scene, SCENE_KEYS):
		return _failure("Room file must contain exactly schemaVersion, companyName, room, and objects.")
	if scene.schemaVersion != 1:
		return _failure("This room file version is not supported.")
	if not _text_within(scene.companyName, 1, 60) or scene.companyName.strip_edges() != scene.companyName:
		return _failure("Company label must be 1–60 trimmed characters with no control characters.")
	if not _same_room(scene.room):
		return _failure("Room geometry must match the trusted Studio demo room.")
	if not scene.objects is Array or scene.objects.size() > 24:
		return _failure("A room can contain at most 24 furniture pieces.")
	var ids := {}
	for item in scene.objects:
		if not _exact_keys(item, OBJECT_KEYS):
			return _failure("Each furniture item must contain exactly id, kind, x, y, width, and height.")
		if not _safe_id(item.id) or ids.has(item.id):
			return _failure("Furniture IDs must be unique, safe names of up to 40 characters.")
		ids[item.id] = true
		if not item.kind is String or not KINDS.has(item.kind):
			return _failure("This furniture type is not available in Room Studio.")
		if not _object_fits(item):
			return _failure("A furniture item must fit fully inside the room boundary.")
	return {"ok": true, "value": scene.duplicate(true), "error": ""}

static func _same_room(room: Variant) -> bool:
	if not _exact_keys(room, ROOM_KEYS) or room.id != "studio-demo" or room.width != 320 or room.height != 224:
		return false
	if not room.polygon is Array or room.polygon.size() != POLYGON.size():
		return false
	for i in range(POLYGON.size()):
		if not room.polygon[i] is Array or room.polygon[i].size() != 2:
			return false
		for axis in range(2):
			if not _number(room.polygon[i][axis]) or room.polygon[i][axis] != POLYGON[i][axis]:
				return false
	return true

static func _object_fits(item: Dictionary) -> bool:
	for key in ["x", "y", "width", "height"]:
		if not _number(item[key]):
			return false
	if item.width < 16 or item.height < 16 or item.width > 320 or item.height > 224:
		return false
	if item.x < 0 or item.y < 0 or item.x + item.width > 320 or item.y + item.height > 224:
		return false
	for point in [Vector2(item.x, item.y), Vector2(item.x + item.width, item.y), Vector2(item.x, item.y + item.height), Vector2(item.x + item.width, item.y + item.height)]:
		if not _point_in_polygon(point):
			return false
	return true

static func _point_in_polygon(point: Vector2) -> bool:
	var sign := 0
	for i in range(POLYGON.size()):
		var a := Vector2(POLYGON[i][0], POLYGON[i][1])
		var b := Vector2(POLYGON[(i + 1) % POLYGON.size()][0], POLYGON[(i + 1) % POLYGON.size()][1])
		var cross := (b.x - a.x) * (point.y - a.y) - (b.y - a.y) * (point.x - a.x)
		if absf(cross) < 0.00000001:
			continue
		var next_sign := 1 if cross > 0 else -1
		if sign != 0 and sign != next_sign:
			return false
		sign = next_sign
	return true

static func _same_catalog(value: Variant) -> bool:
	if not value is Array or value.size() != 1 or not _exact_keys(value[0], ASSET_KEYS):
		return false
	return value[0] == CATALOG[0]

static func _same_attribution(value: Variant) -> bool:
	if not value is Array or value.size() != 1 or not _exact_keys(value[0], ATTRIBUTION_KEYS):
		return false
	return value[0] == ATTRIBUTIONS[0]

static func _exact_keys(value: Variant, expected: Array) -> bool:
	if not value is Dictionary or value.size() != expected.size():
		return false
	for key in expected:
		if not value.has(key):
			return false
	return true

static func _text_within(value: Variant, minimum: int, maximum: int) -> bool:
	if not value is String:
		return false
	var utf16_units := 0
	for i in range(value.length()):
		var codepoint: int = value.unicode_at(i)
		if codepoint <= 31 or (codepoint >= 127 and codepoint <= 159):
			return false
		utf16_units += 2 if codepoint > 0xFFFF else 1
	return utf16_units >= minimum and utf16_units <= maximum

static func _safe_id(value: Variant) -> bool:
	if not value is String or value.is_empty() or value.length() > 40:
		return false
	for i in range(value.length()):
		var c: int = value.unicode_at(i)
		if not ((c >= 48 and c <= 57) or (c >= 65 and c <= 90) or (c >= 97 and c <= 122) or c == 45 or c == 95):
			return false
	return true

static func _number(value: Variant) -> bool:
	return (value is int or value is float) and is_finite(float(value))

static func _is_safe_revision(value: Variant) -> bool:
	return _number(value) and value >= 1 and value <= 9007199254740991 and floorf(float(value)) == float(value)

static func _failure(message: String) -> Dictionary:
	return {"ok": false, "value": null, "error": message}
