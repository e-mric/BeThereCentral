extends RefCounted
class_name ProceduralAvatarArt

const WIDTH := 64
const HEIGHT := 96
const ART_SHA256 := "7522eb485878f943b8ef9ee15f0a3e61cb11e1634613a952b4363552e332d093"
const DEFAULT_APPEARANCE := {
	"bodyPalette": "warm", "hair": "short", "hairPalette": "brown",
	"top": "jacket", "topPalette": "teal", "trousersPalette": "slate", "accessory": "glasses"
}
const RAMP_KEYS := {
	"bodyPalette": "skin", "hairPalette": "hair", "topPalette": "top", "trousersPalette": "trousers"
}

static func load_definition(path: String = "res://assets/community/avatar-parts-v1.json") -> Dictionary:
	var file := FileAccess.open(path, FileAccess.READ)
	if file == null:
		return {"ok": false, "error": "Trusted character art is unavailable.", "value": {}}
	var source := file.get_as_text()
	file.close()
	var digest_context := HashingContext.new()
	digest_context.start(HashingContext.HASH_SHA256)
	digest_context.update(source.to_utf8_buffer())
	if digest_context.finish().hex_encode() != ART_SHA256:
		return {"ok": false, "error": "Trusted character art did not match its pinned definition.", "value": {}}
	var json := JSON.new()
	var parse_result := json.parse(source)
	if parse_result != OK or not json.data is Dictionary:
		return {"ok": false, "error": "Trusted character art could not be read.", "value": {}}
	var value: Dictionary = json.data
	if value.get("version") != 1 or value.get("width") != WIDTH or value.get("height") != HEIGHT or not _same_integer_array(value.get("worldFrameSize"), [32, 48]) or value.get("pixelScale") != 2 or not _same_integer_array(value.get("pivot"), [32, 88]) or value.get("frameDurationMs") != 160:
		return {"ok": false, "error": "Trusted character art has an unsupported format.", "value": {}}
	if not value.get("palettes") is Dictionary or not value.get("appearanceRamps") is Dictionary or not value.get("parts") is Array:
		return {"ok": false, "error": "Trusted character art is incomplete.", "value": {}}
	return {"ok": true, "error": "", "value": value}

static func _same_integer_array(value: Variant, expected: Array) -> bool:
	if not value is Array or value.size() != expected.size():
		return false
	for index in range(expected.size()):
		if not (value[index] is int or value[index] is float) or float(value[index]) != float(expected[index]):
			return false
	return true

static func make_texture(definition: Dictionary, appearance: Dictionary, direction: String, pose: String) -> ImageTexture:
	var image := Image.create(WIDTH, HEIGHT, false, Image.FORMAT_RGBA8)
	image.fill(Color(0, 0, 0, 0))
	var selected_appearance := DEFAULT_APPEARANCE.duplicate()
	for key in appearance:
		selected_appearance[key] = appearance[key]
	for part in definition.parts:
		if not _part_matches(part.select, selected_appearance):
			continue
		for frame in part.frames:
			if frame.direction != direction or frame.pose != pose:
				continue
			var offset: Array = frame.offset
			for run in frame.runs:
				var color_bytes: Array = _resolve_color(definition, selected_appearance, run[3])
				var color := Color8(int(color_bytes[0]), int(color_bytes[1]), int(color_bytes[2]), int(color_bytes[3]))
				image.fill_rect(Rect2i(int(run[0]) + int(offset[0]), int(run[1]) + int(offset[1]), int(run[2]), 1), color)
	return ImageTexture.create_from_image(image)

static func resolve_texture(definition: Dictionary, appearance: Dictionary, direction: String, pose: String, preset_resolver: Callable) -> Texture2D:
	if appearance.has("presetId"):
		if not preset_resolver.is_valid():
			return null
		var preset_texture: Variant = preset_resolver.call(str(appearance.presetId), direction, pose)
		return preset_texture as Texture2D
	return make_texture(definition, appearance, direction, pose)

static func _part_matches(selectors: Dictionary, appearance: Dictionary) -> bool:
	for key in selectors:
		if key == "part" and selectors[key] == "*":
			continue
		if not appearance.has(key) or appearance[key] != selectors[key]:
			return false
	return true

static func _resolve_color(definition: Dictionary, appearance: Dictionary, color_key: String) -> Array:
	var palette_key := "fixed"
	var swatch_key := color_key
	if color_key.begins_with("fixed:"):
		swatch_key = color_key.trim_prefix("fixed:")
	elif color_key.begins_with("@"):
		var components := color_key.trim_prefix("@").split(".")
		if components.size() != 2:
			return [0, 0, 0, 0]
		var appearance_key: String = components[0]
		var ramp_group: String = RAMP_KEYS.get(appearance_key, "")
		var ramp_choices: Dictionary = definition.appearanceRamps.get(ramp_group, {})
		palette_key = str(ramp_choices.get(appearance.get(appearance_key, ""), ""))
		swatch_key = components[1]
	var palettes: Dictionary = definition.palettes
	var palette: Dictionary = palettes.get(palette_key, {})
	return palette.get(swatch_key, [0, 0, 0, 0])

static func raw_sha256(image: Image) -> String:
	var context := HashingContext.new()
	context.start(HashingContext.HASH_SHA256)
	context.update(image.get_data())
	return context.finish().hex_encode()

static func rasterize_preset(source: Image, preset: Dictionary, direction: String, pose: String) -> ImageTexture:
	var directions: Dictionary = preset.get("frames", {})
	var poses: Dictionary = directions.get(direction, {})
	if not poses.has(pose):
		return null
	var frame: Dictionary = poses[pose]
	var crop: Array = frame.crop
	var source_pivot: Array = frame.sourcePivot
	var output := Image.create(WIDTH, HEIGHT, false, Image.FORMAT_RGBA8)
	output.fill(Color(0, 0, 0, 0))
	var foot_pivot: Array = preset.footPivot
	var scale: float = preset.sourceScale
	for y in range(HEIGHT):
		var source_y := int(floor((float(y) - float(foot_pivot[1])) / scale + float(source_pivot[1])))
		if source_y < 0 or source_y >= int(crop[3]):
			continue
		for x in range(WIDTH):
			var source_x := int(floor((float(x) - float(foot_pivot[0])) / scale + float(source_pivot[0])))
			if source_x < 0 or source_x >= int(crop[2]):
				continue
			output.set_pixel(x, y, source.get_pixel(int(crop[0]) + source_x, int(crop[1]) + source_y))
	return ImageTexture.create_from_image(output)
