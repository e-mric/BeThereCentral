extends RefCounted
class_name TrustedCommunityCatalog

static func load_catalog() -> Dictionary:
	var manifest_path := "res://assets/community/catalog-v2.json"
	var json := JSON.new()
	if json.parse(FileAccess.get_file_as_string(manifest_path)) != OK:
		return {"ok": false, "error": "The trusted room asset collection could not be read."}
	var catalog: Variant = json.data
	if not catalog is Dictionary or catalog.get("catalogId") != "betherecentral-community" or catalog.get("catalogRevision") != 2 or catalog.get("attributionId") != "betherecentral-original-community-assets" or not catalog.get("props") is Array or not catalog.get("character") is Dictionary:
		return {"ok": false, "error": "The trusted room asset collection is invalid."}
	var props := {}
	var textures := {}
	var preset_images := {}
	for prop in catalog.props:
		if not prop is Dictionary or not _safe_prop(prop) or props.has(prop.id):
			return {"ok": false, "error": "The trusted room asset collection is invalid."}
		var texture_path: String = "res://assets/community/" + prop.image
		var texture := load(texture_path) as Texture2D
		if texture == null:
			return {"ok": false, "error": "A trusted room illustration could not be loaded."}
		props[prop.id] = prop
		textures[prop.id] = texture
	var presets: Variant = catalog.get("avatarPresets", {})
	if not presets is Dictionary or not _safe_avatar_presets(presets):
		return {"ok": false, "error": "The trusted character art collection is invalid."}
	for preset_id in presets:
		var preset: Dictionary = presets[preset_id]
		var texture_path: String = "res://assets/community/" + preset.image
		var image := Image.new()
		if image.load(ProjectSettings.globalize_path(texture_path)) != OK:
			return {"ok": false, "error": "A trusted character illustration could not be loaded."}
		if image.get_width() <= 0 or image.get_height() <= 0:
			return {"ok": false, "error": "A trusted character illustration is empty."}
		var digest := FileAccess.get_sha256(ProjectSettings.globalize_path(texture_path))
		if digest != preset.sha256:
			return {"ok": false, "error": "A trusted character illustration did not match its pinned source."}
		for direction in preset.frames:
			for pose in preset.frames[direction]:
				var crop: Array = preset.frames[direction][pose].crop
				if int(crop[0]) < 0 or int(crop[1]) < 0 or int(crop[0] + crop[2]) > image.get_width() or int(crop[1] + crop[3]) > image.get_height():
					return {"ok": false, "error": "A trusted character frame is outside its source image."}
		preset_images[preset_id] = image
	return {"ok": true, "catalog": catalog, "props": props, "textures": textures, "preset_images": preset_images, "error": ""}

static func _safe_avatar_presets(presets: Dictionary) -> bool:
	for preset_id in presets:
		var expected_labels := {"founder-v1": "Founder", "patch-v1": "Patch", "ninon-v1": "Ninon", "morgane-v1": "Morgane", "architect-v1": "The Architect"}
		if not expected_labels.has(preset_id):
			return false
		var preset: Variant = presets[preset_id]
		var expected_keys := ["label", "image", "sha256", "frameSize", "worldSize", "footPivot", "frameDurationMs", "sourceScale", "frames", "attribution"]
		if preset_id in ["ninon-v1", "morgane-v1", "architect-v1"]:
			expected_keys.append("realPerson")
		if not preset is Dictionary or not _same_key_set(preset, expected_keys):
			return false
		if preset_id in ["ninon-v1", "morgane-v1", "architect-v1"] and preset.get("realPerson") != true:
			return false
		var expected_label: String = expected_labels[preset_id]
		var expected_image: String = "v1/" + str(preset_id).trim_suffix("-v1") + "-directions-v1.png"
		if preset.get("label") != expected_label or preset.get("image") != expected_image or not _is_sha256(preset.get("sha256")) or not _valid_preset_attribution(str(preset.get("attribution", ""))):
			return false
		if not _same_numeric_array(preset.get("frameSize"), [64, 96]) or not _same_numeric_array(preset.get("worldSize"), [32, 48]) or not _same_numeric_array(preset.get("footPivot"), [32, 88]):
			return false
		var frame_duration: Variant = preset.get("frameDurationMs")
		var source_scale: Variant = preset.get("sourceScale")
		if not (frame_duration is int or frame_duration is float) or not (source_scale is int or source_scale is float):
			return false
		var supports_mixed_architect: bool = preset_id == "architect-v1" and float(frame_duration) == 80.0 and float(source_scale) == 1.0
		var supports_legacy_four_pose: bool = float(frame_duration) == 160.0 and float(source_scale) == 0.32
		if not supports_mixed_architect and not supports_legacy_four_pose:
			return false
		if not preset.get("frames") is Dictionary or preset.frames.size() != 4:
			return false
		var has_eight_pose_direction := false
		for direction in ["front", "back", "left", "right"]:
			if not preset.frames.has(direction) or not preset.frames[direction] is Dictionary:
				return false
			var walk_pose_count: int = preset.frames[direction].size() - 1
			if supports_mixed_architect:
				if walk_pose_count not in [4, 8]:
					return false
				if walk_pose_count == 8:
					has_eight_pose_direction = true
			else:
				if walk_pose_count != 4:
					return false
			var expected_poses := ["idle", "walk1", "walk2", "walk3", "walk4"]
			if walk_pose_count == 8:
				expected_poses = ["idle", "walk1", "walk2", "walk3", "walk4", "walk5", "walk6", "walk7", "walk8"]
			for pose in expected_poses:
				if not preset.frames[direction].has(pose):
					return false
				var frame: Variant = preset.frames[direction][pose]
				if not frame is Dictionary or not frame.get("crop") is Array or frame.crop.size() != 4 or not frame.get("sourcePivot") is Array or frame.sourcePivot.size() != 2:
					return false
				for coordinate in frame.crop:
					if not (coordinate is int or coordinate is float) or not is_finite(float(coordinate)) or float(coordinate) < 0.0 or floor(float(coordinate)) != float(coordinate):
						return false
				for coordinate in frame.sourcePivot:
					if not (coordinate is int or coordinate is float) or not is_finite(float(coordinate)):
						return false
		if supports_mixed_architect and not has_eight_pose_direction:
			return false
	return true

static func _same_key_set(value: Dictionary, expected: Array) -> bool:
	if value.size() != expected.size():
		return false
	for key in expected:
		if not value.has(key):
			return false
	return true

static func _same_numeric_array(value: Variant, expected: Array) -> bool:
	if not value is Array or value.size() != expected.size():
		return false
	for index in range(expected.size()):
		if not (value[index] is int or value[index] is float) or float(value[index]) != float(expected[index]):
			return false
	return true

static func _is_sha256(value: Variant) -> bool:
	if not value is String or value.length() != 64:
		return false
	for character in value:
		if not "0123456789abcdef".contains(character):
			return false
	return true

static func _valid_preset_attribution(attribution: String) -> bool:
	return attribution.begins_with("Original ")

static func _safe_prop(prop: Dictionary) -> bool:
	for key in ["id", "image", "crop", "defaultSize", "minSize", "maxSize", "drawOrder", "collision", "interaction", "sha256"]:
		if not prop.has(key):
			return false
	if not prop.id is String or prop.id.is_empty() or not prop.image is String or not prop.image.begins_with("v1/") or prop.image.contains("..") or prop.image.contains("\\"):
		return false
	if not prop.crop is Array or prop.crop.size() != 4 or not prop.defaultSize is Array or prop.defaultSize.size() != 2 or not prop.minSize is Array or prop.minSize.size() != 2 or not prop.maxSize is Array or prop.maxSize.size() != 2:
		return false
	if prop.drawOrder not in ["floor", "prop"]:
		return false
	return true
