extends SceneTree

const Validator = preload("res://scripts/package_validator.gd")
const V2Validator = preload("res://scripts/package_v2_validator.gd")
const CatalogLoader = preload("res://scripts/trusted_catalog.gd")
const Physics = preload("res://scripts/room_physics.gd")
const ProgressState = preload("res://scripts/local_progress.gd")
const AvatarArt = preload("res://scripts/avatar_art.gd")
const GameMain = preload("res://scripts/main.gd")
const TEST_PROGRESS_DIR := "user://contract_test_progress"

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	var root := ProjectSettings.globalize_path("res://..")
	var catalog_result := CatalogLoader.load_catalog()
	if not catalog_result.ok:
		printerr("Trusted catalog error: ", catalog_result.error)
	_check(catalog_result.ok and catalog_result.props.size() == 24, "trusted catalog resolves 24 bundled props")
	var fixtures := root.path_join("content/community/v1")
	var v2_fixtures := root.path_join("content/community/v2")
	var acceptance: Dictionary = JSON.parse_string(FileAccess.get_file_as_string(v2_fixtures.path_join("acceptance.json")))
	for filename in acceptance.valid:
		var v2_valid := V2Validator.parse_file(v2_fixtures.path_join(filename), catalog_result.catalog)
		_check(v2_valid.ok, "v2 accepts " + filename + (" (" + v2_valid.error + ")" if not v2_valid.ok else ""))
	for filename in acceptance.invalid:
		var v2_invalid := V2Validator.parse_file(v2_fixtures.path_join("fixtures/invalid").path_join(filename), catalog_result.catalog)
		_check(not v2_invalid.ok, "v2 rejects " + filename)
	var v2_good := V2Validator.parse_file(v2_fixtures.path_join("package-play-learn-v2.json"), catalog_result.catalog)
	var live_game := GameMain.new()
	live_game.progress.save_directory = TEST_PROGRESS_DIR.path_join("scene-smoke")
	get_root().add_child(live_game)
	await process_frame
	live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
	var direction_cases := [[Vector2.DOWN, "front"], [Vector2.UP, "back"], [Vector2.LEFT, "left"], [Vector2.RIGHT, "right"]]
	var manual_appearance: Dictionary = AvatarArt.DEFAULT_APPEARANCE
	var expected_poses := ["walk-a", "walk-b", "walk-c", "walk-d"]
	for direction_case in direction_cases:
		live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
		var observed_poses: Array[String] = []
		for _frame in range(4):
			live_game._apply_movement_input(direction_case[0], 8.0 / 68.0)
			observed_poses.append(live_game._avatar_pose_for_phase(manual_appearance, live_game.walk_phase))
		var all_poses_seen := observed_poses.size() == expected_poses.size()
		for expected_pose in expected_poses:
			all_poses_seen = all_poses_seen and observed_poses.has(expected_pose)
		_check(GameMain.AVATAR_DIRECTIONS[live_game.facing] == direction_case[1] and all_poses_seen, "%s movement selects facing and advances through all four walk poses per 32 pixels" % direction_case[1])
		live_game._apply_movement_input(Vector2.ZERO, 0.16)
		_check(not live_game.walking and live_game._avatar_pose_for_phase(manual_appearance, -1) == "idle" and live_game.walk_phase == 0 and is_zero_approx(live_game.animation_distance), "%s movement stops on the idle pose" % direction_case[1])
	live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
	for _frame in range(4):
		live_game._apply_movement_input(Vector2.RIGHT, 8.0 / 68.0)
	var reference_chunked_end: Vector2 = live_game.avatar_position
	live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
	for _frame in range(16):
		live_game._apply_movement_input(Vector2.RIGHT, 2.0 / 68.0)
	_check(live_game.avatar_position.distance_to(reference_chunked_end) <= 0.001 and live_game.walk_phase == 0 and is_zero_approx(live_game.animation_distance), "equal travel across smaller frame chunks yields the same walk phase and position")
	live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
	live_game._apply_movement_input(Vector2(1, -1), 0.5)
	var diagonal_distance := Vector2(64, 160).distance_to(live_game.avatar_position)
	_check(is_equal_approx(diagonal_distance, 34.0) and live_game.walk_phase == 0 and is_equal_approx(live_game.animation_distance, 2.0), "diagonal walking is normalized and animation follows Euclidean travel distance")
	live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
	live_game.avatar_position = Vector2(46, 86)
	live_game.walk_phase = 2
	live_game.animation_distance = 5.0
	live_game._apply_movement_input(Vector2.RIGHT, 0.5)
	_check(GameMain.AVATAR_DIRECTIONS[live_game.facing] == "right" and not live_game.walking and live_game.walk_phase == 0 and is_zero_approx(live_game.animation_distance), "blocked movement still faces its input but stays idle against an obstacle")
	var unsupported_presets: Dictionary = catalog_result.catalog.avatarPresets.duplicate(true)
	for direction in ["front", "back", "left", "right"]:
		unsupported_presets["architect-v1"].frames[direction]["walk9"] = unsupported_presets["architect-v1"].frames[direction].walk1.duplicate(true)
	_check(not CatalogLoader._safe_avatar_presets(unsupported_presets), "trusted catalog rejects unsupported avatar pose counts")
	var negative_crop_catalog: Dictionary = catalog_result.catalog.avatarPresets.duplicate(true)
	negative_crop_catalog["architect-v1"].frames.front.idle.crop[0] = -1
	_check(not CatalogLoader._safe_avatar_presets(negative_crop_catalog), "trusted catalog rejects negative avatar crop coordinates")
	var fractional_crop_catalog: Dictionary = catalog_result.catalog.avatarPresets.duplicate(true)
	fractional_crop_catalog["architect-v1"].frames.front.idle.crop[0] = 0.5
	_check(not CatalogLoader._safe_avatar_presets(fractional_crop_catalog), "trusted catalog rejects fractional avatar crop coordinates")
	var nonfinite_crop_catalog: Dictionary = catalog_result.catalog.avatarPresets.duplicate(true)
	nonfinite_crop_catalog["architect-v1"].frames.front.idle.crop[0] = NAN
	_check(not CatalogLoader._safe_avatar_presets(nonfinite_crop_catalog), "trusted catalog rejects non-finite avatar crop coordinates")
	var nonfinite_pivot_catalog: Dictionary = catalog_result.catalog.avatarPresets.duplicate(true)
	nonfinite_pivot_catalog["architect-v1"].frames.front.idle.sourcePivot[0] = NAN
	_check(not CatalogLoader._safe_avatar_presets(nonfinite_pivot_catalog), "trusted catalog rejects non-finite avatar source pivots")
	_check(live_game._avatar_walk_pose_count({"presetId": "founder-v1"}) == 4, "legacy trusted presets retain four walking phases")
	_check(live_game._avatar_walk_pose_count({"presetId": "architect-v1"}) == 8, "Architect preset uses eight walking phases")
	var architect_direction_cases := [[Vector2.DOWN, "front"], [Vector2.UP, "back"], [Vector2.LEFT, "left"], [Vector2.RIGHT, "right"]]
	for direction_case in architect_direction_cases:
		live_game._load_package(v2_fixtures.path_join("package-founders-v2.json"))
		_set_player_appearance(live_game, {"presetId": "architect-v1"})
		var expected_phase_count: int = {"front": 8, "back": 4, "left": 8, "right": 8}[direction_case[1]]
		var pixels_per_pose := 32.0 / float(expected_phase_count)
		var observed_architect_poses: Array[String] = []
		for _phase in range(expected_phase_count):
			live_game._apply_movement_input(direction_case[0], pixels_per_pose / 68.0)
			observed_architect_poses.append(live_game._avatar_pose_for_phase({"presetId": "architect-v1"}, live_game.walk_phase, direction_case[1]))
		var expected_architect_sequence: Array[String] = []
		for pose_number in range(2, expected_phase_count + 1):
			expected_architect_sequence.append("walk%d" % pose_number)
		expected_architect_sequence.append("walk1")
		_check(live_game._avatar_walk_pose_count({"presetId": "architect-v1"}, direction_case[1]) == expected_phase_count and GameMain.AVATAR_DIRECTIONS[live_game.facing] == direction_case[1] and observed_architect_poses == expected_architect_sequence, "Architect %s movement advances through its %d walk poses in order per 32 pixels" % [direction_case[1], expected_phase_count])
		live_game._apply_movement_input(Vector2.ZERO, 0.0)
		_check(not live_game.walking and live_game.walk_phase == 0 and is_zero_approx(live_game.animation_distance), "Architect %s stop resets its walk cycle" % direction_case[1])
	live_game._load_package(v2_fixtures.path_join("package-founders-v2.json"))
	_set_player_appearance(live_game, {"presetId": "architect-v1"})
	for _frame in range(8):
		live_game._apply_movement_input(Vector2.RIGHT, 4.0 / 68.0)
	var architect_reference_end: Vector2 = live_game.avatar_position
	live_game._load_package(v2_fixtures.path_join("package-founders-v2.json"))
	_set_player_appearance(live_game, {"presetId": "architect-v1"})
	for _frame in range(16):
		live_game._apply_movement_input(Vector2.RIGHT, 2.0 / 68.0)
	_check(live_game.avatar_position.distance_to(architect_reference_end) <= 0.001 and live_game.walk_phase == 0 and is_zero_approx(live_game.animation_distance), "Architect eight-pose cycle is invariant to frame chunking over equal distance")
	var six_pose_catalog: Dictionary = catalog_result.catalog.avatarPresets.duplicate(true)
	for direction in ["front", "left", "right"]:
		six_pose_catalog["architect-v1"].frames[direction].erase("walk7")
		six_pose_catalog["architect-v1"].frames[direction].erase("walk8")
	_check(not CatalogLoader._safe_avatar_presets(six_pose_catalog), "trusted catalog rejects an unsupported six-pose direction")
	live_game._load_package(v2_fixtures.path_join("package-founders-v2.json"))
	_set_player_appearance(live_game, {"presetId": "architect-v1"})
	live_game.avatar_position = Vector2(46, 86)
	live_game._apply_movement_input(Vector2.RIGHT, 0.5)
	_check(not live_game.walking and live_game.walk_phase == 0 and is_zero_approx(live_game.animation_distance), "blocked Architect input does not animate")
	live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
	live_game._apply_movement_input(Vector2.RIGHT, 8.0 / 68.0)
	live_game._apply_movement_input(Vector2.ZERO, 0.0)
	live_game._apply_movement_input(Vector2.RIGHT, 8.0 / 68.0)
	_check(live_game.walk_phase == 1 and is_zero_approx(live_game.animation_distance), "stopping then restarting begins a fresh walk cycle")
	var resolved_founder: Texture2D = AvatarArt.resolve_texture(live_game.avatar_art_definition, {"presetId": "founder-v1"}, "front", "idle", Callable(live_game, "_make_trusted_preset_texture"))
	_check(resolved_founder != null and AvatarArt.raw_sha256(resolved_founder.get_image()) == JSON.parse_string(FileAccess.get_file_as_string(root.path_join("content/community/assets/founder-conformance-v1.json"))).hashes.front.idle, "runtime avatar provider resolves a trusted preset to its golden frame")
	for preset_spec in [["ninon", "Ninon", "ninon-v1"], ["morgane", "Morgane", "morgane-v1"], ["architect", "The Architect", "architect-v1"]]:
		live_game._load_package(v2_fixtures.path_join("package-%s-v2.json" % preset_spec[0]))
		var public_character: Dictionary = {}
		for character in live_game.current_package.characters:
			if character.appearance == {"presetId": preset_spec[2]}:
				public_character = character
		_check(public_character.get("displayName") == preset_spec[1] and public_character.get("introduction", "").is_empty() and public_character.get("appearance") == {"presetId": preset_spec[2]}, "%s example uses only the approved name and trusted sprite preset" % preset_spec[1])
		_check(catalog_result.catalog.avatarPresets[preset_spec[2]].get("realPerson") == true, "%s preset is marked as a real-person portrayal" % preset_spec[1])
		var public_sprite: Texture2D = AvatarArt.resolve_texture(live_game.avatar_art_definition, public_character.get("appearance", {}), "front", "idle", Callable(live_game, "_make_trusted_preset_texture"))
		_check(public_sprite != null, "%s trusted character sprite resolves in the game scene" % preset_spec[1])
	live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
	live_game.progress.reset()
	var long_greeting_package: Dictionary = live_game.current_package.duplicate(true)
	long_greeting_package.characters[1].introduction = "G".repeat(160)
	long_greeting_package.quest.introduction = "A".repeat(600)
	_check(V2Validator.validate(long_greeting_package, catalog_result.catalog).ok, "maximum-length guide introductions are valid package content")
	live_game.current_package = long_greeting_package
	live_game._interact_with_guide()
	_check(live_game.answer_panel.visible and live_game.question_heading.text == "Patch" and live_game.question_prompt.text == "G".repeat(160) + "\n\n" + "A".repeat(600), "guide greeting card displays the full authored introductions")
	var greeting_answers_hidden := true
	for button in live_game.answer_buttons:
		greeting_answers_hidden = greeting_answers_hidden and not button.visible
	_check(greeting_answers_hidden and live_game.answer_continue_button.visible, "guide greeting offers Continue without showing answers before clues")
	live_game.answer_continue_button.pressed.emit()
	live_game._load_package(v2_fixtures.path_join("package-play-learn-v2.json"))
	live_game.progress.reset()
	for clue in live_game.current_package.quest.clues:
		live_game.progress.collect_clue(clue.id)
	live_game._open_clue_journal()
	var journal_before_answer: String = live_game.journal_text.text
	_check(live_game.journal_text.text.contains(live_game.current_package.quest.clues[0].text) and live_game.journal_text.text.contains(live_game.current_package.quest.clues[1].text), "clue journal retains full text for both clues")
	live_game.journal_panel.hide()
	live_game._show_question(live_game._character_by_id("guide"))
	_check(live_game.answer_panel.visible, "guide interaction opens the answer panel after both clues")
	var guide_name: String = live_game._character_by_id("guide").displayName
	_check(live_game.question_heading.text.contains(guide_name) and live_game.question_prompt.text == live_game.current_package.quest.question, "guide question content is bound to stable UI labels")
	_check(live_game.answer_labels.size() == 3 and live_game.answer_labels[0].text == live_game.current_package.quest.answers[0].text, "long answer text is rendered in wrapped answer labels")
	var wrong_answer_id := ""
	for answer in live_game.current_package.quest.answers:
		if answer.id != live_game.current_package.quest.correctAnswerId:
			wrong_answer_id = answer.id
			break
	live_game._submit_answer(wrong_answer_id)
	_check(live_game.answer_feedback.text.begins_with("Not quite.") and live_game.progress.found_clue_ids.size() == 2, "wrong answer gives visible feedback and keeps both clues")
	live_game._open_clue_journal()
	_check(live_game.journal_text.text == journal_before_answer, "retry leaves the full clue journal unchanged")
	live_game.journal_panel.hide()
	live_game._submit_answer(live_game.current_package.quest.correctAnswerId)
	_check(live_game.progress.completed and live_game.answer_feedback.text.contains(live_game.current_package.quest.reward.label), "correct answer shows the explanation and local reward")
	var saved_room_position: Vector2 = live_game.avatar_position
	live_game._enter_construct()
	var construct_start: Vector2 = live_game.construct_position
	live_game._apply_movement_input(Vector2.RIGHT, 8.0 / 68.0)
	_check(live_game.construct_position.x > construct_start.x, "White Construct allows movement in its own space")
	_check(live_game.walking and live_game.walk_phase == 1, "White Construct walking animation follows actual movement distance")
	live_game._return_from_construct()
	_check(live_game.avatar_position == saved_room_position and not live_game.in_construct and not live_game.walking and live_game.walk_phase == 0 and is_zero_approx(live_game.animation_distance), "return restores room position and resets walking animation")
	live_game._apply_movement_input(Vector2.RIGHT, 8.0 / 68.0)
	var position_before_static_import := live_game.avatar_position
	live_game._load_package(fixtures.path_join("package-v1.json"))
	_check(live_game.current_version == 1 and not live_game._has_playable_activity() and not GameMain._should_draw_room_actors(live_game.current_version, live_game.current_package), "v1 import opens as a static preview with no actors")
	_check(live_game.avatar_position == position_before_static_import and live_game.help_label.text.contains("preview") and not live_game.walking and live_game.walk_phase == 0 and is_zero_approx(live_game.animation_distance), "v1 preview preserves position and resets animation")
	live_game.walking = true
	live_game._process(1.0)
	_check(live_game.avatar_position == position_before_static_import and not live_game.walking, "v1 static preview cannot move a hidden avatar")
	var expected_v1: Dictionary = Validator.parse_file(fixtures.path_join("package-v1.json")).value
	_check(live_game.current_package == expected_v1, "v1 preview preserves the validated room data exactly")
	live_game._load_package(v2_fixtures.path_join("package-v2.json"))
	_check(live_game.current_version == 2 and live_game.current_package.gameplay == null and not live_game._has_playable_activity() and not GameMain._should_draw_room_actors(live_game.current_version, live_game.current_package), "room-only v2 import opens as a static preview with no actors")
	_check(live_game.avatar_position == position_before_static_import, "room-only v2 preserves position without inventing a spawn")
	var expected_room_only: Dictionary = V2Validator.parse_file(v2_fixtures.path_join("package-v2.json"), catalog_result.catalog).value
	_check(live_game.current_package == expected_room_only, "room-only v2 preview preserves the validated room data exactly")
	for item in live_game.current_package.room.objects:
		if item.kind == "secret-bookshelf":
			live_game.avatar_position = GameMain._shelf_approach(item)
			break
	live_game._interact()
	_check(not live_game.in_construct, "room-only v2 preview keeps the secret interaction inactive")
	live_game._load_package(v2_fixtures.path_join("package-founders-v2.json"))
	var position_before_dialog := live_game.avatar_position
	live_game.file_dialog.visible = true
	var dialog_key := InputEventKey.new()
	dialog_key.pressed = true
	dialog_key.keycode = KEY_D
	live_game._input(dialog_key)
	live_game._process(1.0)
	_check(live_game.avatar_position == position_before_dialog and not live_game.walking, "file dialog suppresses movement and game input")
	live_game.file_dialog.hide()
	var preserved_package: Dictionary = live_game.current_package.duplicate(true)
	var malformed_path := "user://malformed-version-room.json"
	for bad_version in ["[]", "{}"]:
		var malformed_file := FileAccess.open(malformed_path, FileAccess.WRITE)
		malformed_file.store_string("{\"packageVersion\":%s}" % bad_version)
		malformed_file.close()
		live_game._load_package(malformed_path)
		_check(live_game.current_version == 2 and live_game.current_package == preserved_package, "malformed version %s is rejected transactionally" % bad_version)
	DirAccess.remove_absolute(ProjectSettings.globalize_path(malformed_path))
	live_game.progress.reset()
	live_game.queue_free()
	var colocated_package: Dictionary = v2_good.value.duplicate(true)
	colocated_package.gameplay.clues[1].x = colocated_package.gameplay.clues[0].x
	colocated_package.gameplay.clues[1].y = colocated_package.gameplay.clues[0].y
	_check(V2Validator.validate(colocated_package, catalog_result.catalog).ok, "co-located clue targets remain valid when the exact point is reachable")
	_check(GameMain._shelf_approach(v2_good.value.room.objects[1]) == Vector2(272, 100), "secret bookshelf uses the shared front-approach point")
	var reordered_root: Dictionary = _reverse_keys(v2_good.value)
	reordered_root.assets = _reverse_keys(reordered_root.assets)
	_check(V2Validator.validate(reordered_root, catalog_result.catalog).ok, "v2 object key order is irrelevant")
	_check(Physics.can_occupy(Vector2(6, 112), [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]], []), "avatar radius can touch the exact room-edge clearance")
	_check(not Physics.can_occupy(Vector2(5, 112), [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]], []), "avatar radius cannot cross room-edge clearance")
	var desk: Dictionary = V2Validator._prop_by_id(catalog_result.catalog.props, "desk")
	var desk_item := {"kind": "desk", "x": 44, "y": 36, "width": 64, "height": 44}
	_check(not Physics.can_occupy(Vector2(52, 65), ROOM_POLYGON, [desk_item], catalog_result.catalog.props), "normalized desk collision blocks avatar approach")
	_check(Physics.can_occupy(Vector2(45, 65), ROOM_POLYGON, [desk_item], catalog_result.catalog.props), "avatar can approach outside normalized collision")
	var avatar_definition_result := AvatarArt.load_definition()
	_check(avatar_definition_result.ok, "trusted original avatar-parts definition loads")
	var avatar_definition: Dictionary = avatar_definition_result.value
	var avatar_matrix_path := root.path_join("content/community/assets/avatar-conformance-v1.json")
	var avatar_matrix: Dictionary = JSON.parse_string(FileAccess.get_file_as_string(avatar_matrix_path))
	for direction in avatar_definition.directions:
		for pose in avatar_definition.poses:
			var frame: Image = AvatarArt.make_texture(avatar_definition, AvatarArt.DEFAULT_APPEARANCE, direction, pose).get_image()
			_check(frame.get_width() == 64 and frame.get_height() == 96, "pixel avatar %s/%s frame is 64 by 96 source pixels" % [direction, pose])
	for character_name in avatar_matrix.hashes:
		var appearance: Dictionary = avatar_matrix.appearances[character_name]
		for direction in avatar_matrix.hashes[character_name]:
			for pose in avatar_matrix.hashes[character_name][direction]:
				var conformance_image: Image = AvatarArt.make_texture(avatar_definition, appearance, direction, pose).get_image()
				_check(AvatarArt.raw_sha256(conformance_image) == avatar_matrix.hashes[character_name][direction][pose], "pixel art bytes match %s/%s/%s reference" % [character_name, direction, pose])
	for preset_id in catalog_result.catalog.avatarPresets:
		var preset_metadata: Dictionary = catalog_result.catalog.avatarPresets[preset_id]
		var matrix_path := root.path_join("content/community/assets/%s-conformance-v1.json" % preset_id.trim_suffix("-v1"))
		var preset_matrix: Dictionary = JSON.parse_string(FileAccess.get_file_as_string(matrix_path))
		for direction in preset_matrix.hashes:
			for pose in preset_matrix.hashes[direction]:
				var preset_frame: Image = AvatarArt.rasterize_preset(catalog_result.preset_images[preset_id], preset_metadata, direction, pose).get_image()
				var actual_preset_hash := AvatarArt.raw_sha256(preset_frame)
				if actual_preset_hash != preset_matrix.hashes[direction][pose]:
					printerr("Preset hash mismatch ", preset_id, " ", direction, "/", pose, ": ", actual_preset_hash, " expected ", preset_matrix.hashes[direction][pose])
				_check(actual_preset_hash == preset_matrix.hashes[direction][pose], "%s preset bytes match %s/%s reference" % [preset_id, direction, pose])
	var founder_package := V2Validator.parse_file(v2_fixtures.path_join("package-founder-v2.json"), catalog_result.catalog)
	_check(founder_package.ok and founder_package.value.characters[0].appearance == {"presetId": "founder-v1"}, "founder preset package preserves its trusted preset reference")
	var founders_package := V2Validator.parse_file(v2_fixtures.path_join("package-founders-v2.json"), catalog_result.catalog)
	_check(founders_package.ok and founders_package.value.characters[0].appearance == {"presetId": "founder-v1"} and founders_package.value.characters[1].appearance == {"presetId": "patch-v1"}, "sample room resolves both trusted character presets")
	var local_progress = ProgressState.new()
	local_progress.save_directory = TEST_PROGRESS_DIR.path_join("domain")
	var test_quest := {"id": "codex-contract-test", "clues": [{"id": "one"}, {"id": "two"}], "reward": {"id": "badge"}}
	local_progress.load_saved(test_quest)
	local_progress.reset()
	_check(not local_progress.submit_answer("wrong", "right", ["one", "two"], "badge"), "wrong answer is recorded before the clues are found")
	_check(not local_progress.completed, "wrong answer before clues leaves quest incomplete")
	_check(local_progress.collect_clue("one"), "first clue is recorded")
	_check(not local_progress.collect_clue("one"), "collecting the same clue twice is idempotent")
	_check(local_progress.collect_clue("two"), "second clue is recorded")
	local_progress.load_saved(test_quest)
	_check(local_progress.found_clue_ids.size() == 2 and local_progress.attempts == 1, "valid local progress reloads with clues and attempt count")
	_check(local_progress.found_clue_ids.size() == 2, "answer attempts preserve found clues")
	_check(local_progress.submit_answer("right", "right", ["one", "two"], "badge"), "correct answer completes the quest")
	_check(not local_progress.submit_answer("right", "right", ["one", "two"], "badge"), "completed reward cannot be granted twice")
	_check(local_progress.reward_ids.size() == 1, "reward remains a single local entry")
	local_progress.reset()
	_check(not local_progress.completed and local_progress.found_clue_ids.is_empty(), "reset clears local quest progress")
	local_progress.load_saved(test_quest)
	_check(local_progress.found_clue_ids.is_empty() and local_progress.attempts == 0, "reset progress is persisted as a fresh record")
	var early_quest := test_quest.duplicate(true)
	early_quest.id = "codex-early-answer-test"
	local_progress.load_saved(early_quest)
	local_progress.reset()
	_check(local_progress.submit_answer("right", "right", ["one", "two"], "badge") and not local_progress.completed, "a correct answer can be accepted before clue collection")
	_check(local_progress.reward_ids.is_empty(), "early correct answer does not award before both clues")
	local_progress.collect_clue("one")
	_check(not local_progress.completed, "one clue is insufficient after an accepted answer")
	local_progress.collect_clue("two")
	_check(local_progress.completed and local_progress.reward_ids == ["badge"], "the second clue completes and awards an earlier accepted answer")
	var float_attempt_record := local_progress.to_record()
	float_attempt_record.attempts = 1.0
	_check(local_progress._is_valid_record(float_attempt_record), "integral JSON number representation remains a valid attempt count")
	var changed_quest := test_quest.duplicate(true)
	changed_quest.title = "Updated activity"
	local_progress.load_saved(changed_quest)
	_check(local_progress.found_clue_ids.is_empty() and local_progress.attempts == 0, "edited quest content does not inherit prior progress")
	var interactive_rect := {"x": 20, "y": 30, "width": 60, "height": 40}
	_check(GameMain._near_prop(Vector2(50, 94), interactive_rect, 24.0), "runtime prop interaction uses the validated nearest-rectangle range")
	_check(not GameMain._near_prop(Vector2(50, 95), interactive_rect, 24.0), "runtime prop interaction rejects points outside the validated range")
	var colocated_clues := [{"clueId": "one", "x": 100, "y": 100}, {"clueId": "two", "x": 100, "y": 100}]
	var co_located_targets := {"clues": colocated_clues, "npc": {"x": 100, "y": 100}}
	var first_targets: Dictionary = GameMain._play_interaction_targets(Vector2(100, 100), co_located_targets, [])
	_check(first_targets.clues.size() == 2 and first_targets.npc, "co-located clues and guide are available at one standing point")
	var next_targets: Dictionary = GameMain._play_interaction_targets(Vector2(100, 100), co_located_targets, ["one", "two"])
	_check(next_targets.clues.is_empty() and next_targets.npc, "guide remains reachable after co-located clues are collected")
	var paint_order := GameMain._sort_paint_elements([
		{"type": "character", "floor": false, "depth": 80, "order": 2},
		{"type": "prop", "floor": false, "depth": 72, "order": 0},
		{"type": "prop", "floor": true, "depth": 200, "order": 1},
		{"type": "character", "floor": false, "depth": 80, "order": 3}
	])
	_check(paint_order[0].floor and paint_order[1].depth == 72, "floor art paints first, then objects by foot depth")
	_check(paint_order[2].order == 2 and paint_order[3].order == 3, "equal-depth actors retain deterministic stable order")
	var id_ties := GameMain._sort_paint_elements([
		{"type": "prop", "floor": false, "depth": 20, "tieId": "z-last", "order": 0},
		{"type": "prop", "floor": false, "depth": 20, "tieId": "a-first", "order": 1}
	])
	_check(id_ties[0].tieId == "a-first" and id_ties[1].tieId == "z-last", "equal-depth elements sort by stable ID before insertion order")
	var good := Validator.parse_file(fixtures.path_join("package-v1.json"))
	_check(good.ok, "default package is accepted")
	if not good.ok:
		printerr("Default package error: ", good.error)
		quit(1)
		return
	_check(good.value.packageId == "betherecentral.sample-room", "package ID is preserved")
	_check(good.value.room.room.width == 320 and good.value.room.room.height == 224, "room dimensions are preserved")
	_check(good.value.room.objects.size() == 4, "furniture list is preserved")
	var reordered: Dictionary = good.value.duplicate(true)
	reordered.assets[0] = _reverse_keys(reordered.assets[0])
	reordered.attribution[0] = _reverse_keys(reordered.attribution[0])
	_check(Validator.validate(reordered).ok, "asset and attribution key order is irrelevant")
	reordered.assets[0].license = "Unknown"
	_check(not Validator.validate(reordered).ok, "modified trusted asset metadata is rejected")
	for filename in ["too-many-objects.json", "duplicate-id.json", "unsafe-asset-path.json", "extra-field.json", "unsupported-room-version.json", "unknown-feature.json"]:
		var result := Validator.parse_file(fixtures.path_join("fixtures/invalid").path_join(filename))
		_check(not result.ok, "rejects " + filename)
	var value: Dictionary = good.value.duplicate(true)
	value.contentRevision = 0
	_check(not Validator.validate(value).ok, "revision must be positive")
	value = good.value.duplicate(true)
	value.room.objects[0].x = 300
	_check(not Validator.validate(value).ok, "object corners must stay inside chamfered room")
	value = good.value.duplicate(true)
	value.room.companyName = "😀".repeat(31)
	_check(not Validator.validate(value).ok, "text limit counts UTF-16 code units")
	value.room.companyName = "😀".repeat(30)
	_check(Validator.validate(value).ok, "60 UTF-16 code units are accepted")
	var oversized := " ".repeat(65_537)
	var temp_path := "user://oversized-package-test.json"
	var file := FileAccess.open(temp_path, FileAccess.WRITE)
	file.store_string(oversized)
	file.close()
	_check(not Validator.parse_file(temp_path).ok, "oversized source is rejected before JSON parsing")
	DirAccess.remove_absolute(ProjectSettings.globalize_path(temp_path))
	if failures == 0:
		print("Godot contract checks passed (%d assertions)." % assertions)
	else:
		printerr("Godot contract checks failed: ", failures)
	quit(0 if failures == 0 else 1)

var failures := 0
var assertions := 0
const ROOM_POLYGON := [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]]

func _check(condition: bool, description: String) -> void:
	assertions += 1
	if condition:
		print("PASS: ", description)
	else:
		failures += 1
		printerr("FAIL: ", description)

func _reverse_keys(value: Dictionary) -> Dictionary:
	var keys := value.keys()
	keys.reverse()
	var result := {}
	for key in keys:
		result[key] = value[key]
	return result

func _set_player_appearance(game: Node, appearance: Dictionary) -> void:
	var player_id: String = game.current_package.gameplay.playerCharacterId
	for index in range(game.current_package.characters.size()):
		if game.current_package.characters[index].id == player_id:
			game.current_package.characters[index]["appearance"] = appearance.duplicate(true)
			return
