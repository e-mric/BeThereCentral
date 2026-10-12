export type DecodedPng = { width: number; height: number; rgba: Uint8Array };

function u32(bytes: Uint8Array, offset: number): number {
  return new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength).getUint32(offset, false);
}

function paeth(a: number, b: number, c: number): number {
  const p = a + b - c, pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
  return pa <= pb && pa <= pc ? a : pb <= pc ? b : c;
}

/** Decodes the trusted non-interlaced RGBA8 PNGs used for exact shared avatar pixels. */
export async function decodePngRgba(bytes: Uint8Array): Promise<DecodedPng> {
  const signature = [137, 80, 78, 71, 13, 10, 26, 10];
  if (signature.some((value, index) => bytes[index] !== value)) throw new Error('Avatar image is not a PNG.');
  let width = 0, height = 0, validFormat = false;
  const chunks: Uint8Array[] = [];
  for (let offset = 8; offset + 12 <= bytes.length;) {
    const length = u32(bytes, offset);
    const type = new TextDecoder().decode(bytes.subarray(offset + 4, offset + 8));
    const data = bytes.subarray(offset + 8, offset + 8 + length);
    if (offset + 12 + length > bytes.length) throw new Error('Avatar PNG has a truncated chunk.');
    if (type === 'IHDR') {
      width = u32(data, 0); height = u32(data, 4);
      validFormat = data[8] === 8 && data[9] === 6 && data[10] === 0 && data[11] === 0 && data[12] === 0;
    } else if (type === 'IDAT') chunks.push(data);
    else if (type === 'IEND') break;
    offset += 12 + length;
  }
  if (!validFormat || width < 1 || height < 1) throw new Error('Avatar PNG must be non-interlaced 8-bit RGBA.');
  const compressedLength = chunks.reduce((sum, chunk) => sum + chunk.length, 0);
  const compressed = new Uint8Array(compressedLength);
  let cursor = 0;
  for (const chunk of chunks) { compressed.set(chunk, cursor); cursor += chunk.length; }
  const stream = new Blob([compressed]).stream().pipeThrough(new DecompressionStream('deflate'));
  const filtered = new Uint8Array(await new Response(stream).arrayBuffer());
  const stride = width * 4, expected = height * (stride + 1);
  if (filtered.length !== expected) throw new Error('Avatar PNG pixel data has an unexpected length.');
  const rgba = new Uint8Array(width * height * 4);
  for (let y = 0; y < height; y += 1) {
    const filter = filtered[y * (stride + 1)], scan = y * (stride + 1) + 1, row = y * stride, previous = row - stride;
    if (filter > 4) throw new Error('Avatar PNG uses an unsupported row filter.');
    for (let x = 0; x < stride; x += 1) {
      const raw = filtered[scan + x], left = x >= 4 ? rgba[row + x - 4] : 0;
      const above = y > 0 ? rgba[previous + x] : 0, upperLeft = y > 0 && x >= 4 ? rgba[previous + x - 4] : 0;
      const predictor = filter === 0 ? 0 : filter === 1 ? left : filter === 2 ? above : filter === 3 ? Math.floor((left + above) / 2) : paeth(left, above, upperLeft);
      rgba[row + x] = (raw + predictor) & 0xff;
    }
  }
  return { width, height, rgba };
}
