import { updatePackage, type StudioPackage } from './content.ts';

/** Make one validated nearby copy while retaining the selected piece's authored size. */
export function duplicatePropNear(value: StudioPackage, selectedId: string): StudioPackage | null {
  if (value.room.objects.length >= 24) return null;
  const source = value.room.objects.find(item => item.id === selectedId);
  if (!source) return null;

  const used = new Set(value.room.objects.map(item => item.id));
  let suffix = 1;
  let id = `${source.kind}-copy-${suffix}`;
  while (used.has(id)) id = `${source.kind}-copy-${++suffix}`;

  const stepX = source.width + 8;
  const stepY = source.height + 8;
  const existing = value.room.objects;
  const overlaps = (x: number, y: number) => existing.some(item =>
    x < item.x + item.width && x + source.width > item.x &&
    y < item.y + item.height && y + source.height > item.y
  );

  for (let ring = 1; ring <= 4; ring += 1) {
    const candidates: Array<[number, number]> = [
      [stepX * ring, 0], [-stepX * ring, 0], [0, stepY * ring], [0, -stepY * ring],
      [stepX * ring, stepY], [-stepX * ring, stepY],
      [stepX * ring, -stepY], [-stepX * ring, -stepY],
      [stepX, stepY * ring], [-stepX, stepY * ring],
      [stepX, -stepY * ring], [-stepX, -stepY * ring]
    ];
    for (const [dx, dy] of candidates) {
      const x = source.x + dx;
      const y = source.y + dy;
      if (overlaps(x, y)) continue;
      try {
        return updatePackage(value, draft => draft.room.objects.push({
          id, kind: source.kind, x, y, width: source.width, height: source.height
        }));
      } catch { /* Keep looking while the trusted outline rules out this position. */ }
    }
  }
  return null;
}
