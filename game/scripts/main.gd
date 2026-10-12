extends Node2D

const ValidatorV1 = preload("res://scripts/package_validator.gd")
const ValidatorV2 = preload("res://scripts/package_v2_validator.gd")
const CatalogLoader = preload("res://scripts/trusted_catalog.gd")
const AvatarArt = preload("res://scripts/avatar_art.gd")
const Physics = preload("res://scripts/room_physics.gd")
const Progress = preload("res://scripts/local_progress.gd")
const FIXTURE_V1 := "../content/community/v1/package-v1.json"
const FIXTURE_V2 := "../content/community/v2/package-founders-v2.json"
const MAX_PACKAGE_BYTES := 65_536
const ROOM_POLYGON := [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]]
const CONSTRUCT_POLYGON := [[0, 0], [320, 0], [320, 224], [0, 224]]
const AVATAR_DIRECTIONS := ["front", "back", "left", "right"]
const AVATAR_WALK_POSES := ["walk-a", "walk-b", "walk-c", "walk-d"]
const V1_ATLAS_PATH := "res://assets/campus_props.png"
const V1_CROPS := {
	"desk": Rect2i(40, 88, 256, 219), "plant": Rect2i(670, 340, 232, 279),
	"sofa": Rect2i(950, 1010, 274, 162), "rug": Rect2i(329, 970, 286, 235)
}

var current_package: Dictionary = {}
var current_version := 1
var atlas_v1: Texture2D
var catalog: Dictionary = {}
var catalog_props: Dictionary = {}
var catalog_textures: Dictionary = {}
var avatar_preset_images: Dictionary = {}
var avatar_art_definition: Dictionary = {}
var avatar_textures: Dictionary = {}
var avatar_position := Vector2(154, 104)
var return_position := Vector2(154, 104)
var construct_position := Vector2(160, 196)
var facing := 0
var walking := false
var walk_phase := 0
var animation_distance := 0.0
var in_construct := false
var interaction_message := ""
var progress = Progress.new()

var status_label: Label
var path_label: Label
var help_label: Label
var progress_label: Label
var title_label: Label
var subtitle_label: Label
var file_dialog: FileDialog
var gallery_panel: PanelContainer
var answer_panel: PanelContainer
var journal_panel: PanelContainer
var journal_text: Label
var answer_feedback: Label
var answer_buttons: Array[Button] = []
var answer_labels: Array[Label] = []
var question_heading: Label
var question_prompt: Label
var answer_continue_button: Button
var return_button: Button

func _ready() -> void:
	texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	var avatar_art_result := AvatarArt.load_definition()
	if avatar_art_result.ok:
		avatar_art_definition = avatar_art_result.value
	else:
		push_error(avatar_art_result.error)
	atlas_v1 = load(V1_ATLAS_PATH)
	var catalog_result := CatalogLoader.load_catalog()
	if catalog_result.ok:
		catalog = catalog_result.catalog
		catalog_props = catalog_result.props
		catalog_textures = catalog_result.textures
		avatar_preset_images = catalog_result.preset_images
	else:
		push_error(catalog_result.error)
	_build_ui()
	progress.load_saved("")
	var requested := _fixture_path(FIXTURE_V2)
	if not FileAccess.file_exists(requested):
		requested = _fixture_path(FIXTURE_V1)
	var args := OS.get_cmdline_user_args()
	if args.is_empty():
		for arg in OS.get_cmdline_args():
			if arg.begins_with("--package=") or arg == "--catalog":
				args.append(arg)
	for arg in args:
		if arg.begins_with("--package="):
			requested = arg.trim_prefix("--package=")
		elif arg == "--catalog":
			call_deferred("_open_gallery")
	_load_package(requested)
	queue_redraw()

func _build_ui() -> void:
	var layer := CanvasLayer.new()
	add_child(layer)
	var ui := Control.new()
	ui.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	ui.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	layer.add_child(ui)
	title_label = Label.new()
	title_label.text = "BETHERECENTRAL  /  LEARNING ROOM"
	title_label.position = Vector2(24, 16)
	title_label.add_theme_font_size_override("font_size", 20)
	title_label.add_theme_color_override("font_color", Color("f2ddbd"))
	ui.add_child(title_label)
	subtitle_label = Label.new()
	subtitle_label.text = "A fictional room for learning and making things together"
	subtitle_label.position = Vector2(25, 45)
	subtitle_label.add_theme_color_override("font_color", Color("aebbc5"))
	ui.add_child(subtitle_label)
	var buttons := HBoxContainer.new()
	buttons.position = Vector2(24, 77)
	buttons.add_theme_constant_override("separation", 8)
	ui.add_child(buttons)
	var load_button := Button.new()
	load_button.text = "Open room…"
	load_button.pressed.connect(_open_dialog)
	buttons.add_child(load_button)
	var reset_button := Button.new()
	reset_button.text = "Restore sample"
	reset_button.pressed.connect(func(): _load_package(_fixture_path(FIXTURE_V2)))
	buttons.add_child(reset_button)
	var gallery_button := Button.new()
	gallery_button.text = "Browse props"
	gallery_button.pressed.connect(_open_gallery)
	buttons.add_child(gallery_button)
	var progress_reset := Button.new()
	progress_reset.text = "Reset progress"
	progress_reset.pressed.connect(_reset_progress)
	buttons.add_child(progress_reset)
	var journal_button := Button.new()
	journal_button.text = "Clue journal"
	journal_button.pressed.connect(_open_clue_journal)
	buttons.add_child(journal_button)
	path_label = Label.new()
	path_label.position = Vector2(24, 116)
	path_label.add_theme_color_override("font_color", Color("aebbc5"))
	ui.add_child(path_label)
	progress_label = Label.new()
	progress_label.position = Vector2(24, 140)
	progress_label.add_theme_color_override("font_color", Color("f2ddbd"))
	ui.add_child(progress_label)
	status_label = Label.new()
	status_label.anchor_left = 0.0
	status_label.anchor_top = 1.0
	status_label.anchor_right = 0.82
	status_label.anchor_bottom = 1.0
	status_label.offset_left = 24
	status_label.offset_top = -56
	status_label.offset_right = 0
	status_label.offset_bottom = -24
	status_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	status_label.add_theme_color_override("font_color", Color("9bd3b5"))
	ui.add_child(status_label)
	help_label = Label.new()
	help_label.anchor_left = 0.0
	help_label.anchor_top = 1.0
	help_label.anchor_right = 1.0
	help_label.anchor_bottom = 1.0
	help_label.offset_left = 24
	help_label.offset_top = -26
	help_label.offset_right = -24
	help_label.offset_bottom = -6
	help_label.text = "Move with WASD or arrow keys · Press E to interact · Esc returns from the secret room"
	help_label.add_theme_color_override("font_color", Color("aebbc5"))
	ui.add_child(help_label)
	return_button = Button.new()
	return_button.text = "Return to room"
	return_button.anchor_left = 1.0
	return_button.anchor_right = 1.0
	return_button.offset_left = -178
	return_button.offset_top = 20
	return_button.offset_right = -24
	return_button.offset_bottom = 60
	return_button.visible = false
	return_button.pressed.connect(_return_from_construct)
	ui.add_child(return_button)
	file_dialog = FileDialog.new()
	file_dialog.file_mode = FileDialog.FILE_MODE_OPEN_FILE
	file_dialog.access = FileDialog.ACCESS_FILESYSTEM
	file_dialog.add_filter("*.json", "Room files")
	file_dialog.file_selected.connect(_load_package)
	ui.add_child(file_dialog)
	_build_answer_panel(ui)
	_build_clue_journal(ui)
	_build_gallery(ui)

func _build_answer_panel(ui: Control) -> void:
	answer_panel = PanelContainer.new()
	answer_panel.anchor_left = 0.5
	answer_panel.anchor_top = 0.5
	answer_panel.anchor_right = 0.5
	answer_panel.anchor_bottom = 0.5
	answer_panel.offset_left = -300
	answer_panel.offset_top = -205
	answer_panel.offset_right = 300
	answer_panel.offset_bottom = 205
	answer_panel.visible = false
	var panel_style := StyleBoxFlat.new()
	panel_style.bg_color = Color("28343d")
	panel_style.border_color = Color("71838a")
	panel_style.set_border_width_all(2)
	panel_style.set_corner_radius_all(8)
	panel_style.content_margin_left = 18
	panel_style.content_margin_top = 16
	panel_style.content_margin_right = 18
	panel_style.content_margin_bottom = 16
	panel_style.shadow_color = Color(0.04, 0.06, 0.08, 0.65)
	panel_style.shadow_size = 10
	answer_panel.add_theme_stylebox_override("panel", panel_style)
	ui.add_child(answer_panel)
	var scroll := ScrollContainer.new()
	scroll.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	scroll.size_flags_vertical = Control.SIZE_EXPAND_FILL
	scroll.horizontal_scroll_mode = ScrollContainer.SCROLL_MODE_DISABLED
	answer_panel.add_child(scroll)
	var stack := VBoxContainer.new()
	stack.add_theme_constant_override("separation", 7)
	stack.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	scroll.add_child(stack)
	question_heading = Label.new()
	question_heading.add_theme_font_size_override("font_size", 17)
	question_heading.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	stack.add_child(question_heading)
	question_prompt = Label.new()
	question_prompt.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	stack.add_child(question_prompt)
	answer_feedback = Label.new()
	answer_feedback.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	answer_feedback.add_theme_color_override("font_color", Color("f2ddbd"))
	stack.add_child(answer_feedback)
	answer_buttons.clear()
	for index in range(3):
		var button := Button.new()
		button.name = "Answer%d" % index
		button.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		button.custom_minimum_size.y = 56
		button.pressed.connect(_submit_answer_from_slot.bind(index))
		stack.add_child(button)
		answer_buttons.append(button)
		var answer_label := Label.new()
		answer_label.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
		answer_label.offset_left = 12
		answer_label.offset_top = 4
		answer_label.offset_right = -12
		answer_label.offset_bottom = -4
		answer_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		answer_label.mouse_filter = Control.MOUSE_FILTER_IGNORE
		button.add_child(answer_label)
		answer_labels.append(answer_label)
	answer_continue_button = Button.new()
	answer_continue_button.text = "Continue"
	answer_continue_button.pressed.connect(func(): answer_panel.hide())
	stack.add_child(answer_continue_button)

func _build_gallery(ui: Control) -> void:
	gallery_panel = PanelContainer.new()
	gallery_panel.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	gallery_panel.offset_left = 16
	gallery_panel.offset_top = 16
	gallery_panel.offset_right = -16
	gallery_panel.offset_bottom = -16
	gallery_panel.visible = false
	var gallery_style := StyleBoxFlat.new()
	gallery_style.bg_color = Color("28343d")
	gallery_style.border_color = Color("71838a")
	gallery_style.set_border_width_all(2)
	gallery_style.set_corner_radius_all(8)
	gallery_style.content_margin_left = 18
	gallery_style.content_margin_top = 16
	gallery_style.content_margin_right = 18
	gallery_style.content_margin_bottom = 16
	gallery_style.shadow_color = Color(0.04, 0.06, 0.08, 0.65)
	gallery_style.shadow_size = 10
	gallery_panel.add_theme_stylebox_override("panel", gallery_style)
	ui.add_child(gallery_panel)
	var outer := VBoxContainer.new()
	outer.add_theme_constant_override("separation", 10)
	gallery_panel.add_child(outer)
	var header := HBoxContainer.new()
	outer.add_child(header)
	var title := Label.new()
	title.text = "ROOM PROPS"
	title.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	title.add_theme_font_size_override("font_size", 18)
	title.add_theme_color_override("font_color", Color("f2f5f4"))
	header.add_child(title)
	var close := Button.new()
	close.text = "Close"
	close.add_theme_color_override("font_color", Color("f2f5f4"))
	close.add_theme_color_override("font_hover_color", Color("ffffff"))
	close.pressed.connect(func(): gallery_panel.hide())
	header.add_child(close)
	var scroll := ScrollContainer.new()
	scroll.size_flags_vertical = Control.SIZE_EXPAND_FILL
	outer.add_child(scroll)
	var grid := GridContainer.new()
	grid.columns = 4
	grid.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	grid.add_theme_constant_override("h_separation", 10)
	grid.add_theme_constant_override("v_separation", 10)
	scroll.add_child(grid)
	if catalog.is_empty():
		var missing := Label.new()
		missing.text = "Room props could not be loaded."
		grid.add_child(missing)
		return
	for prop in catalog.props:
		var card := PanelContainer.new()
		card.custom_minimum_size = Vector2(215, 148)
		var card_style := StyleBoxFlat.new()
		card_style.bg_color = Color("35434b")
		card_style.border_color = Color("56666e")
		card_style.set_border_width_all(1)
		card_style.set_corner_radius_all(5)
		card_style.content_margin_left = 8
		card_style.content_margin_top = 8
		card_style.content_margin_right = 8
		card_style.content_margin_bottom = 8
		card.add_theme_stylebox_override("panel", card_style)
		grid.add_child(card)
		var content := VBoxContainer.new()
		content.alignment = BoxContainer.ALIGNMENT_CENTER
		card.add_child(content)
		var preview := TextureRect.new()
		preview.custom_minimum_size = Vector2(190, 102)
		preview.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
		preview.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		preview.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
		var atlas_texture := AtlasTexture.new()
		atlas_texture.atlas = catalog_textures[prop.id]
		atlas_texture.region = Rect2(prop.crop[0], prop.crop[1], prop.crop[2], prop.crop[3])
		preview.texture = atlas_texture
		content.add_child(preview)
		var name := Label.new()
		name.text = prop.label
		name.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
		name.add_theme_color_override("font_color", Color("f2f5f4"))
		content.add_child(name)
		var size_label := Label.new()
		size_label.text = "%d × %d" % [prop.defaultSize[0], prop.defaultSize[1]]
		size_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
		size_label.add_theme_color_override("font_color", Color("aebbc5"))
		content.add_child(size_label)

func _build_clue_journal(ui: Control) -> void:
	journal_panel = PanelContainer.new()
	journal_panel.anchor_left = 0.5
	journal_panel.anchor_top = 0.5
	journal_panel.anchor_right = 0.5
	journal_panel.anchor_bottom = 0.5
	journal_panel.offset_left = -300
	journal_panel.offset_top = -205
	journal_panel.offset_right = 300
	journal_panel.offset_bottom = 205
	journal_panel.visible = false
	var panel_style := StyleBoxFlat.new()
	panel_style.bg_color = Color("28343d")
	panel_style.border_color = Color("71838a")
	panel_style.set_border_width_all(2)
	panel_style.set_corner_radius_all(8)
	panel_style.content_margin_left = 18
	panel_style.content_margin_top = 16
	panel_style.content_margin_right = 18
	panel_style.content_margin_bottom = 16
	journal_panel.add_theme_stylebox_override("panel", panel_style)
	ui.add_child(journal_panel)
	var scroll := ScrollContainer.new()
	scroll.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	scroll.size_flags_vertical = Control.SIZE_EXPAND_FILL
	scroll.horizontal_scroll_mode = ScrollContainer.SCROLL_MODE_DISABLED
	journal_panel.add_child(scroll)
	var stack := VBoxContainer.new()
	stack.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	stack.add_theme_constant_override("separation", 10)
	scroll.add_child(stack)
	var heading := Label.new()
	heading.text = "CLUE JOURNAL"
	heading.add_theme_font_size_override("font_size", 17)
	stack.add_child(heading)
	journal_text = Label.new()
	journal_text.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	journal_text.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	stack.add_child(journal_text)
	var close := Button.new()
	close.text = "Continue"
	close.pressed.connect(func(): journal_panel.hide())
	stack.add_child(close)

func _open_dialog() -> void:
	file_dialog.popup_centered(Vector2i(760, 520))

func _fixture_path(relative: String) -> String:
	return ProjectSettings.globalize_path("res://").path_join(relative).simplify_path()

func _load_package(path: String) -> void:
	var file := FileAccess.open(path, FileAccess.READ)
	if file == null:
		_set_message("Couldn’t open that room file. Your current room is still here.", true)
		return
	if file.get_length() > MAX_PACKAGE_BYTES:
		file.close()
		_set_message("That room file is too large to open. Your current room is still here.", true)
		return
	var source := file.get_as_text()
	file.close()
	if source.to_utf8_buffer().size() > MAX_PACKAGE_BYTES:
		_set_message("That room file is too large to open. Your current room is still here.", true)
		return
	var json := JSON.new()
	if json.parse(source) != OK or not json.data is Dictionary:
		_set_message("That file doesn’t look like a room. Your current room is still here.", true)
		return
	var version_value: Variant = json.data.get("packageVersion", null)
	if not (version_value is int or version_value is float) or not is_finite(float(version_value)) or floor(float(version_value)) != float(version_value) or float(version_value) not in [1.0, 2.0]:
		_set_message("This room version isn’t supported yet. Your current room is still here.", true)
		return
	var version := int(version_value)
	var loaded: Dictionary
	if version == 1:
		loaded = ValidatorV1.validate(json.data)
	elif version == 2 and not catalog.is_empty():
		loaded = ValidatorV2.validate(json.data, catalog)
	else:
		_set_message("This room version isn’t supported yet. Your current room is still here.", true)
		return
	if not loaded.ok:
		_set_message("That room doesn’t match the supported format. Your current room is still here.", true)
		return
	# Replacement happens only after the full version-specific validation succeeds.
	current_package = loaded.value
	current_version = version
	avatar_textures.clear()
	interaction_message = ""
	_reset_avatar_animation()
	answer_panel.hide()
	journal_panel.hide()
	in_construct = false
	_apply_construct_colors(false)
	return_button.hide()
	if current_version == 2 and current_package.gameplay != null:
		avatar_position = Vector2(current_package.gameplay.spawn.x, current_package.gameplay.spawn.y)
		progress.load_saved(current_package.quest, current_package.contentRevision)
	else:
		progress.load_saved({}, 1)
	path_label.text = "Room file: " + path.get_file()
	if _has_playable_activity():
		_set_message("Room loaded. Find the guide and two clues to start the learning activity.", false)
	else:
		_set_message("Static room preview loaded. This file has no playable activity.", false)
		help_label.text = "Room preview · restore a playable sample to walk and interact"
	_update_progress_label()
	queue_redraw()

func _set_message(message: String, error: bool) -> void:
	status_label.text = message
	status_label.add_theme_color_override("font_color", Color("ff9a8b") if error else Color("9bd3b5"))

func _process(delta: float) -> void:
	if file_dialog.visible:
		_reset_avatar_animation()
		return
	if current_package.is_empty() or gallery_panel.visible or answer_panel.visible or journal_panel.visible or not _has_playable_activity():
		_reset_avatar_animation()
		if not current_package.is_empty() and not gallery_panel.visible and not answer_panel.visible and not journal_panel.visible:
			_update_interaction_prompt()
		return
	var movement := Vector2.ZERO
	if Input.is_key_pressed(KEY_A) or Input.is_key_pressed(KEY_LEFT):
		movement.x -= 1
	if Input.is_key_pressed(KEY_D) or Input.is_key_pressed(KEY_RIGHT):
		movement.x += 1
	if Input.is_key_pressed(KEY_W) or Input.is_key_pressed(KEY_UP):
		movement.y -= 1
	if Input.is_key_pressed(KEY_S) or Input.is_key_pressed(KEY_DOWN):
		movement.y += 1
	_apply_movement_input(movement, delta)
	_update_interaction_prompt()
	queue_redraw()

func _apply_movement_input(movement: Vector2, delta: float) -> void:
	if movement.length_squared() > 0.0:
		if absf(movement.x) > absf(movement.y):
			facing = 3 if movement.x > 0.0 else 2
		else:
			facing = 0 if movement.y > 0.0 else 1
		var starting_position := construct_position if in_construct else avatar_position
		var step := movement.normalized() * 68.0 * maxf(delta, 0.0)
		if in_construct:
			_move_construct_axis(Vector2(step.x, 0))
			_move_construct_axis(Vector2(0, step.y))
		else:
			_move_axis(Vector2(step.x, 0))
			_move_axis(Vector2(0, step.y))
		var ending_position := construct_position if in_construct else avatar_position
		_advance_avatar_animation(starting_position.distance_to(ending_position))
	else:
		_reset_avatar_animation()

func _advance_avatar_animation(traveled_distance: float) -> void:
	if traveled_distance <= 0.0001:
		_reset_avatar_animation()
		return
	walking = true
	animation_distance += traveled_distance
	var phase_count := _avatar_walk_pose_count(_player_appearance(), AVATAR_DIRECTIONS[facing])
	var pixels_per_pose := 32.0 / float(phase_count)
	walk_phase %= phase_count
	while animation_distance + 0.00001 >= pixels_per_pose:
		animation_distance = maxf(0.0, animation_distance - pixels_per_pose)
		walk_phase = (walk_phase + 1) % phase_count

func _reset_avatar_animation() -> void:
	walking = false
	walk_phase = 0
	animation_distance = 0.0

func _avatar_pose_for_phase(appearance: Dictionary, phase: int, direction: String = "front") -> String:
	if phase < 0:
		return "idle"
	if appearance.has("presetId"):
		return "walk%d" % ((phase % _avatar_walk_pose_count(appearance, direction)) + 1)
	return AVATAR_WALK_POSES[phase % AVATAR_WALK_POSES.size()]

func _avatar_walk_pose_count(appearance: Dictionary, direction: String = "front") -> int:
	if not appearance.has("presetId"):
		return AVATAR_WALK_POSES.size()
	var presets: Dictionary = catalog.get("avatarPresets", {})
	var preset: Dictionary = presets.get(str(appearance.presetId), {})
	var direction_frames: Dictionary = preset.get("frames", {}).get(direction, {})
	var pose_count := 0
	for pose in direction_frames:
		if str(pose).begins_with("walk"):
			pose_count += 1
	return pose_count if pose_count > 0 else AVATAR_WALK_POSES.size()

func _player_appearance() -> Dictionary:
	var player := _player_character()
	return player.get("appearance", AvatarArt.DEFAULT_APPEARANCE)

func _move_axis(step: Vector2) -> void:
	var next := avatar_position + step
	var room: Dictionary = current_package.room.room
	var collision_props: Array = catalog.props if current_version == 2 and catalog.has("props") else []
	if Physics.can_occupy(next, room.polygon, current_package.room.objects, collision_props):
		avatar_position = next

func _move_construct_axis(step: Vector2) -> void:
	var next := construct_position + step
	if Physics.can_occupy(next, CONSTRUCT_POLYGON, _construct_props(), catalog_props.values()):
		construct_position = next

func _input(event: InputEvent) -> void:
	if not event is InputEventKey or not event.pressed or event.echo:
		return
	if file_dialog.visible:
		get_viewport().set_input_as_handled()
		return
	if in_construct:
		if event.keycode == KEY_ESCAPE or event.keycode == KEY_ENTER or event.keycode == KEY_KP_ENTER:
			_return_from_construct()
			get_viewport().set_input_as_handled()
		return
	if journal_panel.visible:
		if event.keycode == KEY_ESCAPE:
			journal_panel.hide()
		get_viewport().set_input_as_handled()
		return
	if event.keycode == KEY_ESCAPE:
		answer_panel.hide()
		journal_panel.hide()
		gallery_panel.hide()
	elif event.keycode == KEY_E:
		if _has_playable_activity():
			_interact()
	elif event.keycode == KEY_ENTER or event.keycode == KEY_KP_ENTER:
		if answer_panel.visible:
			get_viewport().set_input_as_handled()
	queue_redraw()

func _interact() -> void:
	if current_package.is_empty() or not _has_playable_activity():
		return
	if current_version == 2:
		for item in current_package.room.objects:
			var prop: Dictionary = catalog_props.get(item.kind, {})
			if prop.get("interaction") == "white-construct" and avatar_position.distance_to(_shelf_approach(item)) <= 8.0:
				_enter_construct()
				return
		if current_package.quest != null and current_package.gameplay != null:
			var newly_found: Array[String] = []
			var targets := _play_interaction_targets(avatar_position, current_package.gameplay, progress.found_clue_ids)
			for clue_id in targets.clues:
				if progress.collect_clue(clue_id):
					newly_found.append(clue_id)
			if not newly_found.is_empty():
				var clue_lines: Array[String] = []
				for clue_id in newly_found:
					var clue := _clue_by_id(clue_id)
					clue_lines.append(clue.title + " · " + clue.text)
				_set_message("\n\n".join(clue_lines), false)
				_update_progress_label()
				if newly_found.size() > 1:
					_open_clue_journal()
				return
			if targets.npc:
				_interact_with_guide()
				return
		for item in current_package.room.objects:
			var prop: Dictionary = catalog_props.get(item.kind, {})
			if prop.get("interaction") == "debug-duck" and _near_prop(avatar_position, item, 24.0):
				_set_message("Duck debugging: say what you expected, what happened, then the smallest part you can inspect.", false)
				return
		_set_message("There’s nothing close enough to interact with. Move nearer and press E.", false)

func _has_playable_activity() -> bool:
	return current_version == 2 and not current_package.is_empty() and current_package.get("gameplay") is Dictionary

static func _should_draw_room_actors(version: int, package: Dictionary) -> bool:
	return version == 2 and package.get("gameplay") is Dictionary

func _interact_with_guide() -> void:
	var character: Dictionary = _character_by_id(current_package.gameplay.npc.characterId)
	if progress.completed:
		_set_message("%s: You earned the %s. You can keep exploring." % [character.displayName, current_package.quest.reward.label], false)
		return
	var found_both := true
	for clue in current_package.quest.clues:
		if not progress.found_clue_ids.has(clue.id):
			found_both = false
	if not found_both:
		_show_guide_greeting(character)
		_set_message("%s: Here’s what to look for." % character.displayName, false)
		return
	_show_question(character)

func _show_guide_greeting(character: Dictionary) -> void:
	question_heading.text = character.displayName
	var paragraphs: Array[String] = []
	var character_introduction := str(character.get("introduction", "")).strip_edges()
	if not character_introduction.is_empty():
		paragraphs.append(character_introduction)
	paragraphs.append(str(current_package.quest.introduction))
	question_prompt.text = "\n\n".join(paragraphs)
	answer_feedback.hide()
	for button in answer_buttons:
		button.hide()
	answer_continue_button.show()
	answer_panel.show()

func _show_question(character: Dictionary) -> void:
	question_heading.text = current_package.quest.title + " · " + character.displayName
	question_prompt.text = current_package.quest.question
	answer_feedback.text = current_package.quest.hint
	answer_feedback.show()
	for index in range(answer_buttons.size()):
		var answer: Dictionary = current_package.quest.answers[index]
		answer_labels[index].text = answer.text
		var font := ThemeDB.fallback_font
		var answer_font_size := 14
		var answer_width := maxf(240.0, answer_panel.size.x - 64.0)
		var text_size := font.get_multiline_string_size(answer.text, HORIZONTAL_ALIGNMENT_LEFT, answer_width, answer_font_size)
		answer_labels[index].add_theme_font_size_override("font_size", answer_font_size)
		answer_labels[index].custom_minimum_size.y = maxf(40.0, text_size.y + 8.0)
		answer_buttons[index].custom_minimum_size.y = maxf(56.0, text_size.y + 16.0)
		answer_buttons[index].disabled = progress.completed
		answer_buttons[index].visible = true
	answer_continue_button.show()
	answer_panel.show()

func _submit_answer(answer_id: String) -> void:
	if progress.completed:
		return
	var required: Array = []
	for clue in current_package.quest.clues:
		required.append(clue.id)
	var accepted := progress.submit_answer(answer_id, current_package.quest.correctAnswerId, required, current_package.quest.reward.id)
	if accepted:
		if progress.completed:
			answer_feedback.text = current_package.quest.explanation + "\nReward: " + current_package.quest.reward.label
			for button in answer_buttons:
				button.hide()
			_set_message("That’s right. You earned the %s." % current_package.quest.reward.label, false)
		else:
			answer_feedback.text = "That’s right. Find both clues to complete the activity."
			_set_message("That’s right. Find both clues to complete the activity.", false)
	else:
		answer_feedback.text = "Not quite. " + current_package.quest.hint
	_update_progress_label()

func _submit_answer_from_slot(index: int) -> void:
	if current_package.is_empty() or current_version != 2 or current_package.quest == null or index >= current_package.quest.answers.size():
		return
	_submit_answer(current_package.quest.answers[index].id)

func _clue_by_id(clue_id: String) -> Dictionary:
	for clue in current_package.quest.clues:
		if clue.id == clue_id:
			return clue
	return {}

func _character_by_id(character_id: String) -> Dictionary:
	for character in current_package.characters:
		if character.id == character_id:
			return character
	return {}

func _update_interaction_prompt() -> void:
	if in_construct:
		help_label.text = "White Construct · Move with WASD or arrows · Return or Esc to go back"
		return
	if not _has_playable_activity():
		help_label.text = "Room preview · restore a playable sample to walk and interact"
		return
	if current_version == 2:
		for item in current_package.room.objects:
			var prop: Dictionary = catalog_props.get(item.kind, {})
			if prop.get("interaction") == "white-construct" and avatar_position.distance_to(_shelf_approach(item)) <= 8.0:
				help_label.text = "Press E to peek behind the bookshelf"
				return
		if current_package.gameplay != null:
			for clue in current_package.gameplay.clues:
				if avatar_position.distance_to(Vector2(clue.x, clue.y)) <= 22 and not progress.found_clue_ids.has(clue.clueId):
					help_label.text = "Press E to read a clue"
					return
			var npc: Dictionary = current_package.gameplay.npc
			if avatar_position.distance_to(Vector2(npc.x, npc.y)) <= 24:
				help_label.text = "Press E to talk to " + _character_by_id(npc.characterId).displayName
				return
		for item in current_package.room.objects:
			var prop: Dictionary = catalog_props.get(item.kind, {})
			if prop.get("interaction") == "debug-duck" and _near_prop(avatar_position, item, 24.0):
				help_label.text = "Press E to talk through the problem with the duck"
				return
	help_label.text = "Move with WASD or arrow keys · Press E to interact · Esc returns from the secret room"

static func _near_prop(position: Vector2, item: Dictionary, radius: float) -> bool:
	var bounds := Rect2(item.x, item.y, item.width, item.height)
	var closest := Vector2(clampf(position.x, bounds.position.x, bounds.end.x), clampf(position.y, bounds.position.y, bounds.end.y))
	return position.distance_to(closest) <= radius

static func _shelf_approach(item: Dictionary) -> Vector2:
	return Vector2(floor((item.x + item.width * 0.5) / 4.0 + 0.5) * 4.0, ceil((item.y + item.height + 8) / 4.0) * 4.0)

static func _nearby_clue_ids(position: Vector2, locations: Array, radius: float = 22.0) -> Array[String]:
	var result: Array[String] = []
	for location in locations:
		if position.distance_to(Vector2(location.x, location.y)) <= radius:
			result.append(location.clueId)
	return result

static func _play_interaction_targets(position: Vector2, gameplay: Dictionary, found_clue_ids: Array, clue_radius: float = 22.0, npc_radius: float = 24.0) -> Dictionary:
	var available_clues: Array[String] = []
	for location in gameplay.get("clues", []):
		if not found_clue_ids.has(location.clueId) and position.distance_to(Vector2(location.x, location.y)) <= clue_radius:
			available_clues.append(location.clueId)
	var npc: Dictionary = gameplay.get("npc", {})
	var npc_near := not npc.is_empty() and position.distance_to(Vector2(npc.x, npc.y)) <= npc_radius
	return {"clues": available_clues, "npc": npc_near}

func _update_progress_label() -> void:
	if current_version != 2 or current_package.quest == null:
		progress_label.text = "Static room preview · props from the trusted community catalog"
		return
	var found: int = progress.found_clue_ids.size()
	if progress.completed:
		progress_label.text = "Learning activity complete · %s" % current_package.quest.reward.label
	else:
		progress_label.text = "%s · clues %d/2" % [current_package.quest.title, found]

func _reset_progress() -> void:
	if current_package.is_empty() or current_version != 2 or current_package.quest == null:
		_set_message("This sample has no learning progress to reset.", false)
		return
	progress.reset()
	answer_panel.hide()
	journal_panel.hide()
	for button in answer_buttons:
		button.show()
		button.disabled = false
	_update_progress_label()
	_set_message("Learning progress has been reset on this device.", false)

func _enter_construct() -> void:
	return_position = avatar_position
	_reset_avatar_animation()
	in_construct = true
	answer_panel.hide()
	return_button.show()
	status_label.text = "A small, fictional room hidden behind the shelf."
	_apply_construct_colors(true)
	help_label.text = "White Construct · Move with WASD or arrows · Return or Esc to go back"
	queue_redraw()

func _return_from_construct() -> void:
	if not in_construct:
		return
	_reset_avatar_animation()
	in_construct = false
	avatar_position = return_position
	return_button.hide()
	_apply_construct_colors(false)
	_set_message("You’re back in the learning room.", false)
	_update_interaction_prompt()
	queue_redraw()

func _apply_construct_colors(enabled: bool) -> void:
	var main_color := Color("34363c") if enabled else Color("f2ddbd")
	var secondary_color := Color("50545c") if enabled else Color("aebbc5")
	var status_color := Color("3d4148") if enabled else Color("9bd3b5")
	for label in [title_label, progress_label]:
		label.add_theme_color_override("font_color", main_color)
	for label in [subtitle_label, path_label, help_label]:
		label.add_theme_color_override("font_color", secondary_color)
	status_label.add_theme_color_override("font_color", status_color)

func _open_gallery() -> void:
	gallery_panel.show()

func _open_clue_journal() -> void:
	if current_version != 2 or current_package.quest == null:
		journal_text.text = "This room doesn’t have any clues yet."
	else:
		journal_text.text = _format_clue_journal(current_package.quest, progress.found_clue_ids)
	journal_panel.show()

static func _format_clue_journal(quest: Dictionary, found_clue_ids: Array) -> String:
	var lines: Array[String] = []
	for clue in quest.get("clues", []):
		if found_clue_ids.has(clue.id):
			lines.append(clue.title + "\n" + clue.text)
	return "\n\n".join(lines) if not lines.is_empty() else "No clues found yet. Explore the room and press E near a clue."

func _draw() -> void:
	if current_package.is_empty():
		return
	var scene: Dictionary = current_package.room
	var room: Dictionary = scene.room
	var viewport := get_viewport_rect().size
	var scale_factor := minf((viewport.x - 120.0) / float(room.width), (viewport.y - 245.0) / float(room.height))
	scale_factor = maxf(1.0, floorf(scale_factor))
	var origin := Vector2((viewport.x - room.width * scale_factor) * 0.5, 174.0 + maxf(0, (viewport.y - 245.0 - room.height * scale_factor) * 0.5))
	if in_construct:
		_draw_construct(origin, scale_factor, room, viewport)
	else:
		_draw_room(origin, scale_factor, room, scene, viewport)

func _draw_room(origin: Vector2, scale_factor: float, room: Dictionary, scene: Dictionary, viewport: Vector2) -> void:
	var polygon: PackedVector2Array = []
	for point in room.polygon:
		polygon.append(origin + Vector2(point[0], point[1]) * scale_factor)
	draw_colored_polygon(polygon, Color("25343a"))
	draw_colored_polygon(polygon, Color("946b50"))
	_draw_wood_seams(origin, scale_factor, room)
	var closed_polygon := polygon.duplicate()
	closed_polygon.append(polygon[0])
	draw_polyline(closed_polygon, Color("394c56"), 9.0, false)
	for i in [0, 1, 2, 7]:
		draw_line(polygon[i] + Vector2(0, 2), polygon[(i + 1) % polygon.size()] + Vector2(0, 2), Color("71838a"), 2.0, false)
	var paint_elements: Array[Dictionary] = []
	var stable_order := 0
	for item in scene.objects:
		paint_elements.append({"type": "prop", "item": item, "floor": _draw_order(item) == "floor", "depth": item.y + item.height, "tieId": str(item.id), "order": stable_order})
		stable_order += 1
	if _should_draw_room_actors(current_version, current_package):
		_draw_clues(origin, scale_factor)
		var npc_data: Dictionary = current_package.gameplay.npc
		paint_elements.append({"type": "character", "character": _character_by_id(npc_data.characterId), "position": Vector2(npc_data.x, npc_data.y), "direction": 2, "phase": -1, "floor": false, "depth": npc_data.y, "tieId": str(npc_data.characterId), "order": stable_order})
		stable_order += 1
	if _should_draw_room_actors(current_version, current_package):
		var player_character := _player_character()
		paint_elements.append({"type": "character", "character": player_character, "position": avatar_position, "direction": facing, "phase": walk_phase if walking else -1, "floor": false, "depth": avatar_position.y, "tieId": str(player_character.id), "order": stable_order})
	for element in _sort_paint_elements(paint_elements):
		if element.type == "prop":
			_draw_prop(element.item, origin, scale_factor)
		else:
			_draw_character(element.character, element.position, element.direction, element.phase, origin, scale_factor)
	var nearby_guide := false
	var guide_position := Vector2(-1000, -1000)
	if current_version == 2 and current_package.gameplay != null:
		guide_position = Vector2(current_package.gameplay.npc.x, current_package.gameplay.npc.y)
		nearby_guide = avatar_position.distance_to(guide_position) <= 40.0
	for element in paint_elements:
		if element.type == "character":
			var label_offset := -12.0 if nearby_guide and element.character.id == str(current_package.gameplay.playerCharacterId) else 0.0
			_draw_character_label(element.character, element.position, origin, scale_factor, label_offset)
	var label := str(scene.companyName)
	var font := ThemeDB.fallback_font
	var label_size := font.get_string_size(label, HORIZONTAL_ALIGNMENT_CENTER, -1, 17)
	draw_string(font, Vector2(viewport.x * 0.5 - label_size.x * 0.5, origin.y - 16), label, HORIZONTAL_ALIGNMENT_LEFT, -1, 17, Color("f2ddbd"))

func _draw_wood_seams(origin: Vector2, scale_factor: float, room: Dictionary) -> void:
	for local_y in range(8, int(room.height), 8):
		var crossings: Array[float] = []
		for i in range(room.polygon.size()):
			var a := Vector2(room.polygon[i][0], room.polygon[i][1])
			var b := Vector2(room.polygon[(i + 1) % room.polygon.size()][0], room.polygon[(i + 1) % room.polygon.size()][1])
			if (a.y <= local_y and b.y > local_y) or (b.y <= local_y and a.y > local_y):
				crossings.append(a.x + (local_y - a.y) * (b.x - a.x) / (b.y - a.y))
		crossings.sort()
		for i in range(0, crossings.size(), 2):
			if i + 1 < crossings.size():
				var seam_y := origin.y + local_y * scale_factor
				draw_line(Vector2(origin.x + crossings[i] * scale_factor, seam_y), Vector2(origin.x + crossings[i + 1] * scale_factor, seam_y), Color(0.32, 0.22, 0.17, 0.42), maxf(1.0, scale_factor * 0.65), false)
	for local_y in range(0, int(room.height), 8):
		for local_x in range(32, int(room.width), 32):
			if _point_in_room(Vector2(local_x, local_y + 4), room.polygon):
				var seam_x := origin.x + local_x * scale_factor
				draw_line(Vector2(seam_x, origin.y + local_y * scale_factor), Vector2(seam_x, origin.y + (local_y + 8) * scale_factor), Color(0.32, 0.22, 0.17, 0.42), maxf(1.0, scale_factor * 0.65), false)

func _draw_prop(item: Dictionary, origin: Vector2, scale_factor: float) -> void:
	var dest := Rect2(origin + Vector2(item.x, item.y) * scale_factor, Vector2(item.width, item.height) * scale_factor)
	if current_version == 1:
		if V1_CROPS.has(item.kind):
			_draw_fitted_region(atlas_v1, V1_CROPS[item.kind], dest)
		return
	var prop: Dictionary = catalog_props.get(item.kind, {})
	if prop.is_empty() or not catalog_textures.has(item.kind):
		return
	var source: Array = prop.crop
	_draw_fitted_region(catalog_textures[item.kind], Rect2i(source[0], source[1], source[2], source[3]), dest)

func _draw_fitted_region(texture: Texture2D, source: Rect2i, destination: Rect2) -> void:
	var fit := minf(destination.size.x / source.size.x, destination.size.y / source.size.y)
	var fitted_size := Vector2(source.size) * fit
	var fitted := Rect2(destination.position + (destination.size - fitted_size) * 0.5, fitted_size)
	draw_texture_rect_region(texture, fitted, source, Color.WHITE, false, true)

func _draw_character(character: Dictionary, feet: Vector2, direction: int, phase: int, origin: Vector2, scale_factor: float) -> void:
	if character.is_empty():
		return
	var appearance: Dictionary = character.get("appearance", AvatarArt.DEFAULT_APPEARANCE)
	var appearance_key := JSON.stringify(_canonical_value(appearance)).sha256_text()
	var pose := _avatar_pose_for_phase(appearance, phase, AVATAR_DIRECTIONS[direction])
	var key: String = str(avatar_art_definition.get("version", 1)) + "-" + appearance_key + "-" + AVATAR_DIRECTIONS[direction] + "-" + pose
	if not avatar_textures.has(key):
		avatar_textures[key] = AvatarArt.resolve_texture(avatar_art_definition, appearance, AVATAR_DIRECTIONS[direction], pose, Callable(self, "_make_trusted_preset_texture"))
	var texture: Texture2D = avatar_textures[key]
	if texture == null:
		return
	var rect := Rect2(origin + Vector2(feet.x - 16, feet.y - 44) * scale_factor, Vector2(32, 48) * scale_factor)
	draw_texture_rect(texture, rect, false)

func _make_trusted_preset_texture(preset_id: String, direction: String, pose: String) -> Texture2D:
	if not catalog.has("avatarPresets") or not catalog.avatarPresets is Dictionary or not catalog.avatarPresets.has(preset_id) or not avatar_preset_images.has(preset_id):
		return null
	return AvatarArt.rasterize_preset(avatar_preset_images[preset_id], catalog.avatarPresets[preset_id], direction, pose)

func _draw_character_label(character: Dictionary, feet: Vector2, origin: Vector2, scale_factor: float, label_offset: float = 0.0) -> void:
	if character.is_empty():
		return
	var rect := Rect2(origin + Vector2(feet.x - 16, feet.y - 44) * scale_factor, Vector2(32, 48) * scale_factor)
	var label_size := ThemeDB.fallback_font.get_string_size(character.displayName, HORIZONTAL_ALIGNMENT_CENTER, -1, 11)
	draw_string(ThemeDB.fallback_font, Vector2(rect.get_center().x - label_size.x * 0.5, rect.position.y - 4 + label_offset), character.displayName, HORIZONTAL_ALIGNMENT_LEFT, -1, 11, Color("25343a"))

static func _sort_paint_elements(elements: Array) -> Array:
	var sorted := elements.duplicate()
	sorted.sort_custom(func(a: Dictionary, b: Dictionary):
		if a.floor != b.floor:
			return a.floor
		if a.depth != b.depth:
			return a.depth < b.depth
		if a.get("tieId", "") != b.get("tieId", ""):
			return str(a.get("tieId", "")) < str(b.get("tieId", ""))
		return a.order < b.order
	)
	return sorted

static func _canonical_value(value: Variant) -> Variant:
	if value is Dictionary:
		var keys: Array = value.keys()
		keys.sort()
		var sorted := {}
		for key in keys:
			sorted[key] = _canonical_value(value[key])
		return sorted
	if value is Array:
		var sorted_array: Array = []
		for item in value:
			sorted_array.append(_canonical_value(item))
		return sorted_array
	return value

func _player_character() -> Dictionary:
	if current_version == 2 and current_package.characters.size() > 0 and current_package.gameplay != null:
		return _character_by_id(current_package.gameplay.playerCharacterId)
	return {"id": "sample-player", "displayName": "You", "appearance": {}}

func _draw_clues(origin: Vector2, scale_factor: float) -> void:
	for index in range(current_package.gameplay.clues.size()):
		var clue_location: Dictionary = current_package.gameplay.clues[index]
		var found: bool = progress.found_clue_ids.has(clue_location.clueId)
		var point := origin + Vector2(clue_location.x, clue_location.y) * scale_factor
		var color := Color("94d6bb") if found else Color("f2ddbd")
		draw_circle(point, 8 * scale_factor, color)
		draw_arc(point, 11 * scale_factor, 0, TAU, 20, Color("283a43"), maxf(1.0, scale_factor), true)
		draw_string(ThemeDB.fallback_font, point + Vector2(-3, 5), "✓" if found else str(index + 1), HORIZONTAL_ALIGNMENT_LEFT, -1, 10 * int(scale_factor), Color("25343a"))

func _draw_construct(origin: Vector2, scale_factor: float, room: Dictionary, viewport: Vector2) -> void:
	draw_rect(Rect2(Vector2.ZERO, viewport), Color("fffdf8"))
	var space := Rect2(origin, Vector2(room.width, room.height) * scale_factor)
	for item in _construct_props():
		_draw_prop(item, origin, scale_factor)
	_draw_character(_player_character(), construct_position, facing, walk_phase if walking else -1, origin, scale_factor)
	_draw_character_label(_player_character(), construct_position, origin, scale_factor)
	var heading := "THE WHITE CONSTRUCT"
	draw_string(ThemeDB.fallback_font, Vector2(space.position.x + 14, space.position.y + 24), heading, HORIZONTAL_ALIGNMENT_LEFT, -1, 14, Color("503b48"))
	draw_string(ThemeDB.fallback_font, Vector2(space.position.x + 14, space.position.y + 44), "A quiet place to think", HORIZONTAL_ALIGNMENT_LEFT, -1, 11, Color("817982"))

func _construct_props() -> Array:
	return [
		{"id": "construct-chair-left", "kind": "burgundy-chair", "x": 84, "y": 124, "width": 34, "height": 42},
		{"id": "construct-chair-right", "kind": "burgundy-chair", "x": 202, "y": 124, "width": 34, "height": 42},
		{"id": "construct-crt", "kind": "crt-television", "x": 143, "y": 74, "width": 34, "height": 40}
	]

func _draw_catalog_kind(kind: String, destination: Rect2) -> void:
	if not catalog_props.has(kind) or not catalog_textures.has(kind):
		return
	var crop: Array = catalog_props[kind].crop
	_draw_fitted_region(catalog_textures[kind], Rect2i(crop[0], crop[1], crop[2], crop[3]), destination)

func _draw_order(item: Dictionary) -> String:
	if current_version == 1:
		return "floor" if item.kind == "rug" else "prop"
	return catalog_props.get(item.kind, {}).get("drawOrder", "prop")

func _point_in_room(point: Vector2, polygon: Array) -> bool:
	var sign := 0
	for i in range(polygon.size()):
		var a := Vector2(polygon[i][0], polygon[i][1])
		var b := Vector2(polygon[(i + 1) % polygon.size()][0], polygon[(i + 1) % polygon.size()][1])
		var cross := (b.x - a.x) * (point.y - a.y) - (b.y - a.y) * (point.x - a.x)
		if absf(cross) < 0.00000001:
			continue
		var next_sign := 1 if cross > 0 else -1
		if sign != 0 and sign != next_sign:
			return false
		sign = next_sign
	return true
