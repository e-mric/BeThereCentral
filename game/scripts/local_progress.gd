extends RefCounted
class_name LocalQuestProgress

const MAX_BYTES := 4096
const SAVE_DIRECTORY := "user://community_progress"
var save_directory := SAVE_DIRECTORY

var quest: Dictionary = {}
var quest_id := ""
var save_key := ""
var required_clue_ids: Array[String] = []
var reward_id := ""
var found_clue_ids: Array[String] = []
var attempts := 0
var correct_answer_accepted := false
var completed := false
var reward_ids: Array[String] = []

func load_saved(quest_definition: Variant, content_revision: int = 1) -> void:
	quest = quest_definition if quest_definition is Dictionary else {}
	quest_id = str(quest.get("id", ""))
	# Keep the public progress record contract stable while isolating saves for edited
	# content that reuses an ID. JSON key order is canonicalized before hashing.
	var canonical_quest := JSON.stringify(_sort_value(quest))
	save_key = quest_id + "-" + str(content_revision) + "-" + canonical_quest.sha256_text().substr(0, 16)
	required_clue_ids.clear()
	for clue in quest.get("clues", []):
		required_clue_ids.append(str(clue.id))
	reward_id = str(quest.get("reward", {}).get("id", ""))
	_reset_memory()
	if quest_id.is_empty() or not FileAccess.file_exists(_save_path()):
		return
	var file := FileAccess.open(_save_path(), FileAccess.READ)
	if file == null or file.get_length() > MAX_BYTES:
		_reset_memory()
		return
	var source := file.get_as_text()
	file.close()
	if source.to_utf8_buffer().size() > MAX_BYTES:
		_reset_memory()
		return
	var json := JSON.new()
	if json.parse(source) != OK or not _is_valid_record(json.data):
		_reset_memory()
		return
	var record: Dictionary = json.data
	found_clue_ids.assign(record.foundClueIds)
	attempts = int(record.attempts)
	correct_answer_accepted = record.correctAnswerAccepted
	completed = record.completed
	reward_ids.assign(record.rewardIds)

func collect_clue(clue_id: String) -> bool:
	if not required_clue_ids.has(clue_id) or found_clue_ids.has(clue_id):
		return false
	found_clue_ids.append(clue_id)
	_update_completion()
	_save()
	return true

func submit_answer(answer_id: String, correct_answer_id: String, _clue_ids: Array, answer_reward_id: String) -> bool:
	if completed:
		return false
	attempts += 1
	if answer_id != correct_answer_id:
		_save()
		return false
	correct_answer_accepted = true
	_update_completion(answer_reward_id)
	_save()
	return true

func _update_completion(answer_reward_id: String = "") -> void:
	var all_clues_found := true
	for clue_id in required_clue_ids:
		if not found_clue_ids.has(clue_id):
			all_clues_found = false
	if correct_answer_accepted and all_clues_found:
		completed = true
		var earned_reward := reward_id if answer_reward_id.is_empty() else answer_reward_id
		if earned_reward == reward_id and not reward_ids.has(reward_id):
			reward_ids.append(reward_id)

func reset() -> void:
	_reset_memory()
	_save()

func to_record() -> Dictionary:
	return {
		"questId": quest_id,
		"foundClueIds": found_clue_ids.duplicate(),
		"attempts": attempts,
		"correctAnswerAccepted": correct_answer_accepted,
		"completed": completed,
		"rewardIds": reward_ids.duplicate()
	}

func _is_valid_record(value: Variant) -> bool:
	var keys := ["questId", "foundClueIds", "attempts", "correctAnswerAccepted", "completed", "rewardIds"]
	if not value is Dictionary or value.size() != keys.size():
		return false
	for key in keys:
		if not value.has(key):
			return false
	if value.questId != quest_id or not value.foundClueIds is Array or not value.rewardIds is Array:
		return false
	if not (value.attempts is int or value.attempts is float) or not is_finite(float(value.attempts)) or floorf(float(value.attempts)) != float(value.attempts) or value.attempts < 0 or value.attempts > 9007199254740991:
		return false
	if not value.correctAnswerAccepted is bool or not value.completed is bool:
		return false
	var seen_clues := {}
	for clue_id in value.foundClueIds:
		if not clue_id is String or not required_clue_ids.has(clue_id) or seen_clues.has(clue_id):
			return false
		seen_clues[clue_id] = true
	var all_clues_found := seen_clues.size() == required_clue_ids.size()
	if value.completed != (value.correctAnswerAccepted and all_clues_found):
		return false
	if value.correctAnswerAccepted and value.attempts < 1:
		return false
	var expected_rewards := [reward_id] if value.completed and not reward_id.is_empty() else []
	if value.rewardIds != expected_rewards:
		return false
	return true

func _reset_memory() -> void:
	found_clue_ids.clear()
	attempts = 0
	correct_answer_accepted = false
	completed = false
	reward_ids.clear()

func _save_path() -> String:
	return save_directory.path_join(save_key + ".json")

func _sort_value(value: Variant) -> Variant:
	if value is Dictionary:
		var keys: Array = value.keys()
		keys.sort()
		var sorted := {}
		for key in keys:
			sorted[key] = _sort_value(value[key])
		return sorted
	if value is Array:
		var sorted_array: Array = []
		for item in value:
			sorted_array.append(_sort_value(item))
		return sorted_array
	return value

func _save() -> void:
	if quest_id.is_empty():
		return
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path(save_directory))
	var file := FileAccess.open(_save_path(), FileAccess.WRITE)
	if file != null:
		file.store_string(JSON.stringify(to_record()))
		file.close()
