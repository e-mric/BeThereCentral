extends RefCounted
class_name RoomPhysics

const PLAYER_FOOTPRINT := Vector2(12, 8)

static func can_occupy(feet: Vector2, polygon: Array, objects: Array, catalog_props: Array = []) -> bool:
	var radius := 6.0
	for i in range(polygon.size()):
		var a := Vector2(polygon[i][0], polygon[i][1])
		var b := Vector2(polygon[(i + 1) % polygon.size()][0], polygon[(i + 1) % polygon.size()][1])
		var edge := b - a
		var cross := edge.x * (feet.y - a.y) - edge.y * (feet.x - a.x)
		if cross < radius * edge.length():
			return false
	for item in objects:
		var prop := _find_prop(catalog_props, item.kind)
		var collision: Variant = prop.get("collision") if not prop.is_empty() else null if item.kind == "rug" else [0.0, 0.0, 1.0, 1.0]
		if collision == null:
			continue
		var obstacle := Rect2(
			float(item.x) + float(collision[0]) * float(item.width),
			float(item.y) + float(collision[1]) * float(item.height),
			float(collision[2]) * float(item.width),
			float(collision[3]) * float(item.height)
		)
		var expanded := obstacle.grow(radius)
		if feet.x >= expanded.position.x and feet.x <= expanded.end.x and feet.y >= expanded.position.y and feet.y <= expanded.end.y:
			return false
	return true

static func _find_prop(catalog_props: Array, prop_id: String) -> Dictionary:
	for prop in catalog_props:
		if prop.id == prop_id:
			return prop
	return {}

static func _inside(point: Vector2, polygon: Array) -> bool:
	var points := PackedVector2Array()
	for vertex in polygon:
		points.append(Vector2(vertex[0], vertex[1]))
	return Geometry2D.is_point_in_polygon(point, points)
