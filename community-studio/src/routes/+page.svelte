<script lang="ts">
  import { onMount } from 'svelte';
  import {
    addProp, avatarPresetCatalog, clonePackage, getCharacterRules, getProp, getAvatarPreset, hitTestObject, makePlayPackage, moveProp, paintElements, parsePackage,
    propCatalog, removeProp, serializePackage, STARTER, updatePackage, updatePlayable,
    type StudioPackage, type Character, type RoomObject
  } from '#lib/content';
  import { MAX_PACKAGE_BYTES } from '../../../content/community/v2/index.js';
  import { advanceAvatarWalkClock, avatarNameplateAnchor, avatarWalkFrameDuration, avatarWalkSequence, avatarWorldRect, renderAvatarPresetRgba, renderAvatarRgba, type AvatarDefinition, type AvatarDirection, type AvatarManualAppearance, type AvatarPose } from '#lib/avatar-art';
  import { decodePngRgba } from '#lib/png-rgba';
  import { duplicatePropNear } from '#lib/workshop';

  const categories = ['Work', 'Play', 'Wellness', 'Meetings', 'Lounge', 'Secrets'] as const;
  const categoryIcons: Record<string, string> = { Work: '▤', Play: '✳', Wellness: '✿', Meetings: '▣', Lounge: '⌂', Secrets: '✦' };
  const catalog = propCatalog();
  const rules = getCharacterRules();
  const avatarPresets = avatarPresetCatalog();
  const manualAppearanceDefaults: AvatarManualAppearance[] = [
    { bodyPalette: 'warm', hair: 'short', hairPalette: 'black', top: 'tee', topPalette: 'teal', trousersPalette: 'slate', accessory: 'none' },
    { bodyPalette: 'deep', hair: 'curly', hairPalette: 'brown', top: 'hoodie', topPalette: 'orange', trousersPalette: 'navy', accessory: 'glasses' }
  ];
  let canvas: HTMLCanvasElement;
  let stage: HTMLDivElement;
  let portraitCanvas = $state<HTMLCanvasElement>();
  let roomPackage = $state<StudioPackage>(clonePackage(STARTER));
  let selectedId = $state<string | null>(STARTER.room.objects[0]?.id ?? null);
  let selectedCategory = $state<string>('Work');
  let activeTab = $state<'Room' | 'People' | 'Quest'>('Room');
  let moveOpen = $state(false);
  let detailsOpen = $state(false);
  let fileMenuOpen = $state(false);
  let fileMenuButton: HTMLButtonElement;
  let selectedCharacter = $state(0);
  let portraitDirection = $state(0);
  let portraitWalking = $state(false);
  let portraitFrame = $state(0);
  let lastWalkFrameAt = 0;
  let walkElapsedMs = 0;
  let avatarDefinition = $state<AvatarDefinition | null>(null);
  const avatarAtlases = new Map<string, { width: number; height: number; rgba: Uint8Array }>();
  const lastCustomCharacter = new Map<string, { displayName: string; introduction: string; appearance: AvatarManualAppearance }>();
  let status = $state('Local editor ready. Save explicitly to keep changes on this device.');
  let fileStatus = $state('');
  let dirty = $state(false);
  let undoStack = $state<StudioPackage[]>([]);
  let redoStack = $state<StudioPackage[]>([]);
  let sprites = new Map<string, HTMLImageElement>();
  let avatarFrames = new Map<string, HTMLCanvasElement>();
  let spriteRevision = $state(0);
  let viewportRevision = $state(0);
  let drag: { id: string; x: number; y: number; px: number; py: number; before: StudioPackage } | null = null;
  const selected = $derived(roomPackage.room.objects.find(item => item.id === selectedId));
  const selectedPerson = $derived(roomPackage.characters[selectedCharacter]);
  const selectedPersonPreset = $derived(selectedPerson && 'presetId' in selectedPerson.appearance ? getAvatarPreset(selectedPerson.appearance.presetId) : null);
  const selectedPersonIsReal = $derived(Boolean(selectedPerson && 'presetId' in selectedPerson.appearance && selectedPersonPreset?.realPerson));
  const propsInCategory = $derived(catalog.filter(prop => prop.category === (selectedCategory === 'Work' ? 'Workspaces' : selectedCategory)));
  const playable = $derived(roomPackage.features.length === 2);
  const STORAGE_KEY = 'betherecentral.community-studio.v2';

  $effect(() => {
    activeTab; selectedPerson; portraitDirection; portraitWalking; portraitFrame; avatarDefinition; spriteRevision; walkPoses;
    if (activeTab === 'People' && portraitCanvas && selectedPerson) drawPortrait();
  });

  function setStatus(value: string) { fileStatus = ''; status = value; }
  function markDirty() { dirty = true; setStatus('Unsaved changes. Save to keep them on this device.'); }
  function rememberManualAppearances(value: StudioPackage) {
    for (const character of value.characters) if (!isPresetAppearance(character.appearance)) lastCustomCharacter.set(character.id, { displayName: character.displayName, introduction: character.introduction, appearance: { ...character.appearance } });
  }
  function redrawCommit(next: StudioPackage | null) {
    if (!next || JSON.stringify(next) === JSON.stringify(roomPackage)) return false;
    undoStack = [...undoStack.slice(-79), clonePackage(roomPackage)]; redoStack = [];
    roomPackage = next; rememberManualAppearances(next); markDirty(); draw(); return true;
  }
  function choose(id: string) { selectedId = id; draw(); }
  function add(kind: string) {
    const next = addProp(roomPackage, kind);
    if (redrawCommit(next)) selectedId = next!.room.objects.at(-1)?.id ?? null;
    else setStatus('There is no available place for that piece. Try moving another piece first.');
    draw();
  }
  function deleteSelected() {
    if (!selectedId) return;
    const next = removeProp(roomPackage, selectedId);
    if (redrawCommit(next)) { selectedId = roomPackage.room.objects.at(-1)?.id ?? null; moveOpen = false; draw(); }
  }
  function duplicateSelected() {
    if (!selected) return;
    const next = duplicatePropNear(roomPackage, selected.id);
    if (redrawCommit(next)) { selectedId = next!.room.objects.at(-1)?.id ?? null; moveOpen = false; detailsOpen = true; draw(); }
    else setStatus('There is no nearby room for a copy. Move this piece first.');
  }
  function moveSelected(dx: number, dy: number) {
    if (!selected) return;
    if (!redrawCommit(moveProp(roomPackage, selected.id, selected.x + dx, selected.y + dy)) && selected) setStatus('That move would place the piece outside the room.');
  }
  function history(direction: 'undo' | 'redo') {
    const source = direction === 'undo' ? undoStack : redoStack;
    const target = direction === 'undo' ? redoStack : undoStack;
    if (!source.length) return;
    const next = source.at(-1)!;
    if (direction === 'undo') { undoStack = source.slice(0, -1); redoStack = [...target, clonePackage(roomPackage)]; }
    else { redoStack = source.slice(0, -1); undoStack = [...target, clonePackage(roomPackage)]; }
    roomPackage = clonePackage(next); rememberManualAppearances(roomPackage); selectedId = roomPackage.room.objects.find(o => o.id === selectedId)?.id ?? roomPackage.room.objects.at(-1)?.id ?? null;
    markDirty(); draw();
  }
  function save() {
    fileStatus = '';
    try { localStorage.setItem(STORAGE_KEY, serializePackage(roomPackage)); dirty = false; status = 'Saved on this device.'; }
    catch { setStatus('Could not save. Check this browser’s storage settings.'); }
  }
  function load() {
    fileStatus = '';
    try {
      const raw = localStorage.getItem(STORAGE_KEY); if (!raw) { setStatus('No saved room is available on this device.'); return; }
      const next = parsePackage(raw); lastCustomCharacter.clear(); roomPackage = next; rememberManualAppearances(next); selectedId = next.room.objects[0]?.id ?? null; undoStack = []; redoStack = []; dirty = false; status = 'Saved room loaded.'; draw();
    } catch (error) { setStatus(`Saved file could not be loaded: ${message(error)}`); }
  }
  type NativeStudioWindow = Window & { BeThereCentralStudio?: { exportPackage: (source: string) => boolean } };
  function nativeExportResult(event: Event) {
    const detail = (event as CustomEvent<unknown>).detail;
    const statuses: Record<string, string> = { success: 'Room file exported.', cancelled: 'Export cancelled. Your room is unchanged.', error: 'Could not export. Your room is unchanged.' };
    if (typeof detail === 'string' && statuses[detail]) fileStatus = statuses[detail];
  }
  function exportFile() {
    const native = (window as NativeStudioWindow).BeThereCentralStudio;
    if (native) {
      try { fileStatus = native.exportPackage(serializePackage(roomPackage)) ? 'Choose where to save your room file.' : 'File export could not start. Try again after the current save finishes.'; }
      catch { fileStatus = 'File export could not start.'; }
      return;
    }
    const blob = new Blob([serializePackage(roomPackage)], { type: 'application/json' }); const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = 'betherecentral-room-v2.json'; a.click(); URL.revokeObjectURL(url); fileStatus = 'Room file exported.';
  }
  async function importFile(event: Event) {
    const input = event.currentTarget as HTMLInputElement; const file = input.files?.[0]; if (!file) return;
    try {
      if (file.size > MAX_PACKAGE_BYTES) throw new Error(`Files must be ${MAX_PACKAGE_BYTES} bytes or smaller.`);
      const next = parsePackage(await file.text());
      if (redrawCommit(next)) { lastCustomCharacter.clear(); rememberManualAppearances(next); selectedId = next.room.objects[0]?.id ?? null; fileStatus = 'Checked file opened. Your current work can be restored with Undo.'; draw(); }
      else fileStatus = 'This file already matches the room on screen.';
    } catch (error) { fileStatus = `File not opened: ${message(error)} Your current room is unchanged.`; }
    input.value = '';
  }
  function message(error: unknown) { return error instanceof Error ? error.message : 'Unexpected file error.'; }
  function ensurePlayable() {
    if (playable) return;
    try {
      const source = makePlayPackage(roomPackage); redrawCommit(source); activeTab = 'People';
    } catch (error) { setStatus(`Could not add the starter activity: ${message(error)}`); }
  }
  function editPackage(change: (draft: StudioPackage) => void) {
    try { redrawCommit(updatePlayable(roomPackage, change)); status = 'Edit checked.'; }
    catch (error) { setStatus(`That edit needs a change: ${message(error)}`); }
  }
  function editText(target: 'character' | 'quest' | 'clue' | 'answer' | 'reward', field: string, value: string, index = 0) {
    editPackage(draft => {
      if (target === 'character') draft.characters[selectedCharacter][field as 'displayName' | 'introduction'] = value;
      if (target === 'quest' && draft.quest) (draft.quest as unknown as Record<string, unknown>)[field] = value;
      if (target === 'clue' && draft.quest) (draft.quest.clues[index] as unknown as Record<string, unknown>)[field] = value;
      if (target === 'answer' && draft.quest) (draft.quest.answers[index] as unknown as Record<string, unknown>)[field] = value;
      if (target === 'reward' && draft.quest) draft.quest.reward[field as 'label'] = value;
    });
  }
  function editAppearance(field: string, value: string) {
    if (!selectedPerson || 'presetId' in selectedPerson.appearance) return;
    const appearance = { ...selectedPerson.appearance, [field]: value } as AvatarManualAppearance;
    const next = updatePlayable(roomPackage, draft => { draft.characters[selectedCharacter].appearance = appearance; });
    if (redrawCommit(next)) lastCustomCharacter.set(selectedPerson.id, { displayName: selectedPerson.displayName, introduction: selectedPerson.introduction, appearance: { ...appearance } });
  }
  function chooseAvatarStyle(style: string) {
    if (!selectedPerson) return;
    if (style !== 'custom') {
      const preset = getAvatarPreset(style);if(!preset)return;
      if (!('presetId' in selectedPerson.appearance)) lastCustomCharacter.set(selectedPerson.id, { displayName: selectedPerson.displayName, introduction: selectedPerson.introduction, appearance: { ...selectedPerson.appearance } });
      editPackage(draft => { const character=draft.characters[selectedCharacter];character.appearance={ presetId: style };if(preset.realPerson){character.displayName=preset.label;character.introduction='';} });
      resetPreviewAnimation();
      return;
    }
    if (!('presetId' in selectedPerson.appearance)) return;
    const remembered = lastCustomCharacter.get(selectedPerson.id);
    const appearance = remembered?.appearance ?? manualAppearanceDefaults[selectedCharacter] ?? manualAppearanceDefaults[0];
    editPackage(draft => { const character=draft.characters[selectedCharacter];character.appearance={ ...appearance };if(remembered){character.displayName=remembered.displayName;character.introduction=remembered.introduction;} });
    resetPreviewAnimation();
  }
  function editPosition(axis: 'x'|'y', raw: string) {
    if (!selected || raw.trim() === '') return;
    const n = Number(raw); const next = Number.isFinite(n) ? moveProp(roomPackage, selected.id, axis === 'x' ? n : selected.x, axis === 'y' ? n : selected.y) : null;
    if (!redrawCommit(next)) setStatus('That position would put part of the piece outside the room.');
  }
  function updateRoomName(value: string) {
    try { redrawCommit(updatePackage(roomPackage, draft => { draft.room.companyName = value.replace(/[\u0000-\u001f\u007f-\u009f]/gu, '').slice(0, 60).trim(); })); }
    catch (error) { setStatus(message(error)); }
  }
  function gamePoint(key:string){const g=roomPackage.gameplay!;if(key==='spawn')return g.spawn;if(key==='npc')return g.npc;return g.clues[Number(key.slice(-1))];}
  function editGamePoint(key:string,axis:'x'|'y',raw:string){if(raw.trim()==='')return;const value=Number(raw);if(Number.isInteger(value))editPackage(d=>{if(!d.gameplay)return;const point=key==='spawn'?d.gameplay.spawn:key==='npc'?d.gameplay.npc:d.gameplay.clues[Number(key.slice(-1))];point[axis]=value;});}

  function insidePolygon(x: number, y: number, polygon: number[][]) {
    let inside = false;
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i, i += 1) { const [xi,yi]=polygon[i], [xj,yj]=polygon[j]; if ((yi>y)!==(yj>y) && x < ((xj-xi)*(y-yi))/(yj-yi)+xi) inside=!inside; }
    return inside;
  }
  function roomTransform() {
    const rect = stage.getBoundingClientRect();
    const scale = Math.max(0.01, Math.min(Math.max(1, rect.width - 32) / 320, Math.max(1, rect.height - 32) / 224));
    return { scale, left:(rect.width-320*scale)/2, top:(rect.height-224*scale)/2 };
  }
  function draw() {
    if (!canvas || !stage) return;
    const rect = stage.getBoundingClientRect(); if (rect.width < 32 || rect.height < 32) return; const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const w = Math.max(1,Math.floor(rect.width*dpr)), h=Math.max(1,Math.floor(rect.height*dpr));
    if (canvas.width!==w || canvas.height!==h) { canvas.width=w; canvas.height=h; }
    const ctx=canvas.getContext('2d'); if(!ctx) return; ctx.clearRect(0,0,w,h); ctx.fillStyle='#111719';ctx.fillRect(0,0,w,h);
    const {scale,left,top}=roomTransform(); const s=scale*dpr; ctx.save();ctx.translate(left*dpr,top*dpr);ctx.scale(s,s);
    const polygon=roomPackage.room.room.polygon;ctx.beginPath();polygon.forEach(([x,y],i)=>i?ctx.lineTo(x,y):ctx.moveTo(x,y));ctx.closePath();ctx.fillStyle='#946b50';ctx.fill();ctx.save();ctx.clip();
    ctx.fillStyle='rgba(63,39,28,.24)';for(let y=8;y<224;y+=12){ctx.fillStyle=y%24===8?'rgba(66,39,27,.33)':'rgba(66,39,27,.2)';ctx.fillRect(0,y,320,1);for(let x=(y%24===8?24:0);x<320;x+=48)if(insidePolygon(x,y+6,polygon))ctx.fillRect(x,y,1,12);}
    const g=playable ? roomPackage.gameplay : null;
    const actors=g?[{id:g.playerCharacterId,x:g.spawn.x,y:g.spawn.y},{id:g.npc.characterId,x:g.npc.x,y:g.npc.y}]:[];
    if(g)g.clues.forEach((c,i)=>drawMarker(ctx,c.x,c.y,String(i+1),'#ed9160'));
    const ordered=paintElements(roomPackage.room.objects,actors);
    for(const element of ordered){
      if(element.type==='prop')drawObject(ctx,element.item);
      else { const character=roomPackage.characters.find(item=>item.id===element.actor.id);if(character)drawAvatar(ctx,character,element.actor.x,element.actor.y,element.actor.id===g?.playerCharacterId?'front':'left','idle'); }
    }
    for(const element of ordered)if(element.type==='character'){const character=roomPackage.characters.find(item=>item.id===element.actor.id);if(character)drawCharacterLabel(ctx,character,element.actor.x,element.actor.y);}
    ctx.restore();ctx.beginPath();polygon.forEach(([x,y],i)=>i?ctx.lineTo(x,y):ctx.moveTo(x,y));ctx.closePath();ctx.strokeStyle='#49372f';ctx.lineWidth=9;ctx.stroke();ctx.strokeStyle='#bf9672';ctx.lineWidth=2;ctx.stroke();
    if(selected){ctx.strokeStyle='#f58242';ctx.lineWidth=1.5;ctx.setLineDash([4,2]);ctx.strokeRect(selected.x-2,selected.y-2,selected.width+4,selected.height+4);ctx.setLineDash([]);ctx.fillStyle='#fff4e8';for(const [x,y] of [[selected.x-3,selected.y-3],[selected.x+selected.width-2,selected.y-3],[selected.x-3,selected.y+selected.height-2],[selected.x+selected.width-2,selected.y+selected.height-2]]){ctx.fillRect(x,y,5,5);ctx.strokeStyle='#f58242';ctx.lineWidth=1;ctx.strokeRect(x,y,5,5);}}
    ctx.restore();
  }
  function drawMarker(ctx: CanvasRenderingContext2D,x:number,y:number,label:string,color:string){ctx.fillStyle=color;ctx.beginPath();ctx.arc(x,y,6,0,Math.PI*2);ctx.fill();ctx.fillStyle='#252421';ctx.font='bold 7px sans-serif';ctx.textAlign='center';ctx.textBaseline='middle';ctx.fillText(label,x,y);}
  function avatarPalette(name:string, colors:Record<string,string>,fallback:string){return colors[name]??fallback;}
  function colorAdjust(hex:string,amount:number,lighten=false){const n=parseInt(hex.slice(1),16);const channels=[(n>>16)&255,(n>>8)&255,n&255].map(c=>Math.round(lighten?c+(255-c)*amount:c*(1-amount)));return `rgb(${channels.join(',')})`;}
  const directions: AvatarDirection[] = ['front','back','left','right'];
  function isPresetAppearance(appearance: Character['appearance']): appearance is { presetId: string } { return 'presetId' in appearance; }
  const walkPoses = $derived(selectedPerson && isPresetAppearance(selectedPerson.appearance)
    ? avatarWalkSequence(getAvatarPreset(selectedPerson.appearance.presetId), directions[portraitDirection])
    : avatarWalkSequence());
  function resetPreviewAnimation(){portraitFrame=0;walkElapsedMs=0;lastWalkFrameAt=performance.now();}
  function frameGeometry(character: Character) {
    if (isPresetAppearance(character.appearance)) {
      const preset = getAvatarPreset(character.appearance.presetId);
      if (!preset) return null;
      return { frameSize: preset.frameSize, worldSize: preset.worldSize, pivot: preset.footPivot };
    }
    if (!avatarDefinition) return null;
    return { frameSize: [avatarDefinition.width, avatarDefinition.height] as [number, number], worldSize: avatarDefinition.worldFrameSize, pivot: avatarDefinition.pivot };
  }
  function getAvatarFrame(character: Character, direction: AvatarDirection, pose: AvatarPose): HTMLCanvasElement | null {
    const geometry=frameGeometry(character);if(!geometry)return null;
    let data: Uint8Array | Uint8ClampedArray;
    let version: string;
    if(isPresetAppearance(character.appearance)){
      const preset=getAvatarPreset(character.appearance.presetId),atlas=avatarAtlases.get(character.appearance.presetId);if(!preset||!atlas)return null;
      data=renderAvatarPresetRgba(character.appearance.presetId,preset,atlas.rgba,atlas.width,atlas.height,direction,pose);version=preset.sha256;
    }else{
      if(!avatarDefinition)return null;
      data=renderAvatarRgba(avatarDefinition,character.appearance,direction,pose);version=String(avatarDefinition.version);
    }
    let appearanceKey:string;
    if(isPresetAppearance(character.appearance))appearanceKey=`preset:${character.appearance.presetId}`;
    else {const manual=character.appearance;appearanceKey=Object.keys(manual).sort().map(key=>`${key}:${manual[key as keyof typeof manual]}`).join('|');}
    const key=`${version}|${appearanceKey}|${direction}|${pose}`;
    const cached=avatarFrames.get(key);if(cached){avatarFrames.delete(key);avatarFrames.set(key,cached);return cached;}
    const frame=document.createElement('canvas');frame.width=geometry.frameSize[0];frame.height=geometry.frameSize[1];
    const frameContext=frame.getContext('2d');if(!frameContext)return null;
    const imageData=frameContext.createImageData(geometry.frameSize[0],geometry.frameSize[1]);imageData.data.set(new Uint8ClampedArray(data));frameContext.putImageData(imageData,0,0);
    avatarFrames.set(key,frame);if(avatarFrames.size>128)avatarFrames.delete(avatarFrames.keys().next().value!);return frame;
  }
  function drawAvatar(ctx:CanvasRenderingContext2D,character:Character,feetX:number,feetY:number,direction:AvatarDirection,pose:AvatarPose){
    const frame=getAvatarFrame(character,direction,pose),geometry=frameGeometry(character);if(!frame||!geometry)return;
    const rect=avatarWorldRect(feetX,feetY,geometry.frameSize,geometry.worldSize,geometry.pivot);
    ctx.imageSmoothingEnabled=false;ctx.drawImage(frame,0,0,geometry.frameSize[0],geometry.frameSize[1],rect.x,rect.y,rect.width,rect.height);
  }
  function drawCharacterLabel(ctx:CanvasRenderingContext2D,character:Character,feetX:number,feetY:number){const geometry=frameGeometry(character);if(!geometry)return;const rect=avatarWorldRect(feetX,feetY,geometry.frameSize,geometry.worldSize,geometry.pivot),anchor=avatarNameplateAnchor(rect);ctx.font='10px sans-serif';ctx.textAlign='center';ctx.textBaseline='bottom';const width=ctx.measureText(character.displayName).width+8;ctx.fillStyle='rgba(37,52,58,.92)';ctx.fillRect(anchor.x-width/2,anchor.y-15,width,15);ctx.fillStyle='#f4e6cf';ctx.fillText(character.displayName,anchor.x,anchor.y-3);}
  function drawPortrait(){
    if(!portraitCanvas||!selectedPerson)return;const geometry=frameGeometry(selectedPerson);if(!geometry)return;const rect=portraitCanvas.getBoundingClientRect(),dpr=Math.min(window.devicePixelRatio||1,2);
    const width=Math.max(1,Math.floor(rect.width*dpr)),height=Math.max(1,Math.floor(rect.height*dpr));if(portraitCanvas.width!==width||portraitCanvas.height!==height){portraitCanvas.width=width;portraitCanvas.height=height;}
    const ctx=portraitCanvas.getContext('2d');if(!ctx)return;ctx.clearRect(0,0,width,height);ctx.imageSmoothingEnabled=false;ctx.save();ctx.scale(dpr,dpr);
    const scale=Math.max(1,Math.floor(Math.min((rect.width-24)/geometry.frameSize[0],(rect.height-12)/geometry.frameSize[1])));
    const pose=portraitWalking?walkPoses[portraitFrame % walkPoses.length]:('idle' as const),frame=getAvatarFrame(selectedPerson,directions[portraitDirection],pose);
    if(frame)ctx.drawImage(frame,0,0,geometry.frameSize[0],geometry.frameSize[1],Math.floor((rect.width-geometry.frameSize[0]*scale)/2),Math.floor((rect.height-geometry.frameSize[1]*scale)/2),geometry.frameSize[0]*scale,geometry.frameSize[1]*scale);
    ctx.restore();
  }
  function drawObject(ctx: CanvasRenderingContext2D,item: RoomObject){
    const prop=getProp(item.kind);if(!prop)return;const image=sprites.get(prop.image),[sx,sy,sw,sh]=prop.crop;
    if(image?.complete&&image.naturalWidth){const factor=Math.min(item.width/sw,item.height/sh),dw=sw*factor,dh=sh*factor,dx=item.x+(item.width-dw)/2,dy=item.y+(item.height-dh)/2;ctx.imageSmoothingEnabled=false;ctx.drawImage(image,sx,sy,sw,sh,dx,dy,dw,dh);}
    else{ctx.fillStyle='#b57957';ctx.fillRect(item.x,item.y,item.width,item.height);}
  }
  function assetUrl(path:string,sha256:string){return `/assets/community/${path}?v=${encodeURIComponent(sha256)}`;}
  function thumbnailStyle(prop: (typeof catalog)[number]) {
    spriteRevision; viewportRevision;
    const [x,y,w,h] = prop.crop;
    const narrow = typeof window !== 'undefined' && window.matchMedia('(max-width: 620px)').matches;
    const compact = typeof window !== 'undefined' && window.matchMedia('(max-width: 800px)').matches;
    const landscape = typeof window !== 'undefined' && window.matchMedia('(max-height: 520px) and (orientation: landscape)').matches;
    const width = narrow ? 90 : compact ? 100 : 112;
    const height = landscape ? 58 : narrow || compact ? 82 : 88;
    const factor = Math.min(width / w, height / h);
    const [iw,ih] = canvasImageSize(prop.image);
    return `width:${w*factor}px;height:${h*factor}px;background-image:url('${assetUrl(prop.image,prop.sha256)}');background-size:${iw*factor}px ${ih*factor}px;background-position:-${x*factor}px -${y*factor}px`;
  }
  function viewportResize() { viewportRevision += 1; draw(); drawPortrait(); }
  function canvasImageSize(path:string):[number,number]{const img=sprites.get(path);return [img?.naturalWidth||1254,img?.naturalHeight||1254];}
  function pointerPoint(event:PointerEvent){const r=stage.getBoundingClientRect(),t=roomTransform();return{x:(event.clientX-r.left-t.left)/t.scale,y:(event.clientY-r.top-t.top)/t.scale};}
  function pointerDown(event:PointerEvent){const p=pointerPoint(event);const item=hitTestObject(roomPackage.room.objects,p.x,p.y);if(!item){selectedId=null;draw();return;}choose(item.id);drag={id:item.id,x:item.x,y:item.y,px:p.x,py:p.y,before:clonePackage(roomPackage)};canvas.setPointerCapture(event.pointerId);}
  function pointerMove(event:PointerEvent){if(!drag)return;const p=pointerPoint(event);const next=moveProp(roomPackage,drag.id,drag.x+p.x-drag.px,drag.y+p.y-drag.py);if(next){roomPackage=next;markDirty();draw();}}
  function pointerUp(){if(!drag)return;if(JSON.stringify(drag.before)!==JSON.stringify(roomPackage)){undoStack=[...undoStack.slice(-79),drag.before];redoStack=[];}drag=null;}
  function keydown(event:KeyboardEvent){if(event.key==='Escape'&&fileMenuOpen){fileMenuOpen=false;fileMenuButton?.focus();return;}const target=event.target as HTMLElement;const editing=['INPUT','TEXTAREA','SELECT'].includes(target.tagName)||target.isContentEditable;if(!editing&&(event.metaKey||event.ctrlKey)&&event.key.toLowerCase()==='z'){event.preventDefault();history(event.shiftKey?'redo':'undo');return;}if(!editing&&(event.metaKey||event.ctrlKey)&&event.key.toLowerCase()==='y'){event.preventDefault();history('redo');return;}if(editing||target.tagName==='BUTTON'||activeTab!=='Room')return;const d:Record<string,[number,number]>={ArrowUp:[0,-4],ArrowDown:[0,4],ArrowLeft:[-4,0],ArrowRight:[4,0]};if(d[event.key]){event.preventDefault();moveSelected(...d[event.key]);}if(event.key==='Delete'||event.key==='Backspace')deleteSelected();}
  onMount(()=>{const paths=[...new Set(catalog.map(p=>p.image))];for(const path of paths){const prop=catalog.find(item=>item.image===path)!;const image=new Image();image.onload=()=>{spriteRevision+=1;draw();};image.src=assetUrl(path,prop.sha256);sprites.set(path,image);}const observer=new ResizeObserver(()=>{draw();drawPortrait();});observer.observe(stage);let timer=0;const avatarLoads=[fetch('/assets/community/avatar-parts-v1.json').then(response=>{if(!response.ok)throw new Error('Shared character artwork could not be loaded.');return response.json();}).then(value=>{avatarDefinition=value as AvatarDefinition;}),...Object.entries(avatarPresets).map(async([id,preset])=>{const response=await fetch(assetUrl(preset.image,preset.sha256));if(!response.ok)throw new Error(`${preset.label} artwork could not be loaded.`);const decoded=await decodePngRgba(new Uint8Array(await response.arrayBuffer()));avatarAtlases.set(id,decoded);})];void Promise.all(avatarLoads).then(()=>{spriteRevision+=1;timer=window.setInterval(()=>{const now=performance.now();if(activeTab!=='People'||!portraitWalking||!selectedPerson){lastWalkFrameAt=now;return;}const preset=isPresetAppearance(selectedPerson.appearance)?getAvatarPreset(selectedPerson.appearance.presetId):null;const duration=avatarWalkFrameDuration(preset,directions[portraitDirection],avatarDefinition?.frameDurationMs??160);const advanced=advanceAvatarWalkClock(portraitFrame,walkElapsedMs+Math.max(0,now-lastWalkFrameAt),duration,walkPoses.length);lastWalkFrameAt=now;walkElapsedMs=advanced.elapsedMs;if(portraitFrame!==advanced.index){portraitFrame=advanced.index;drawPortrait();}},40);draw();drawPortrait();}).catch(error=>{status=message(error);});draw();window.addEventListener('keydown',keydown);window.addEventListener('betherecentral-export-result',nativeExportResult);window.addEventListener('resize',viewportResize);return()=>{observer.disconnect();window.removeEventListener('keydown',keydown);window.removeEventListener('betherecentral-export-result',nativeExportResult);window.removeEventListener('resize',viewportResize);if(timer)window.clearInterval(timer);for(const image of sprites.values())image.removeAttribute('src');sprites.clear();avatarAtlases.clear();avatarFrames.clear();};});
</script>

<svelte:head><title>Studio · BeThereCentral</title><meta name="description" content="Arrange a fictional room, character and learning activity." /></svelte:head>
<div class="studio-app">
  <header class="topbar">
    <div class="brand" aria-label="BeThereCentral Studio"><span class="brand-mark">B</span><span class="brand-copy"><strong>Studio</strong><small>BeThereCentral</small></span></div>
    <div class="room-title"><label for="room-name">Room name</label><input id="room-name" aria-label="Sample room name" maxlength="60" value={roomPackage.room.companyName} onchange={e=>{updateRoomName(e.currentTarget.value);e.currentTarget.value=roomPackage.room.companyName;}}/><span class="room-detail">Fictional sample</span></div>
    <div class="top-actions">
      <button class="icon-button" type="button" aria-label="Undo" title="Undo" disabled={!undoStack.length} onclick={()=>history('undo')}><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 14 4 9l5-5M4 9h9a6 6 0 0 1 0 12h-2"/></svg></button>
      <button class="icon-button" type="button" aria-label="Redo" title="Redo" disabled={!redoStack.length} onclick={()=>history('redo')}><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m15 14 5-5-5-5m5 5h-9a6 6 0 0 0 0 12h2"/></svg></button>
      <button class="save-button" type="button" aria-label="Save on this device" onclick={save}>Save</button>
      <div class="file-menu-wrap"><button class="icon-button menu-trigger" type="button" aria-label="File menu" aria-controls="file-actions-panel" aria-expanded={fileMenuOpen} bind:this={fileMenuButton} onclick={()=>fileMenuOpen=!fileMenuOpen}><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/></svg></button>
        <div id="file-actions-panel" class="file-menu" role="group" aria-label="File actions" hidden={!fileMenuOpen}><button onclick={()=>{load();fileMenuOpen=false;}}>Load saved work</button><button onclick={()=>{exportFile();fileMenuOpen=false;}}>Export room file</button><button onclick={()=>{document.getElementById('package-file')?.click();fileMenuOpen=false;}}>Import room file</button></div>
      </div>
    </div>
    <input id="package-file" class="visually-hidden" type="file" tabindex="-1" aria-label="Import room file" accept="application/json,.json" onchange={e=>{importFile(e);fileMenuOpen=false;}}/>
  </header>
  <div class="workbench" class:shelf-hidden={activeTab!=='Room'}>
    <nav class="side-rail" aria-label="Studio sections">
      <button type="button" class:active={activeTab==='Room'} aria-current={activeTab==='Room'?'page':undefined} onclick={()=>activeTab='Room'}><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 11 12 4l9 7v9h-6v-6H9v6H3z"/><path d="M8 10h8"/></svg><span>Room</span></button>
      <button type="button" class:active={activeTab==='People'} aria-current={activeTab==='People'?'page':undefined} onclick={()=>{ensurePlayable();activeTab='People';}}><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="7" r="4"/><path d="M4 21a8 8 0 0 1 16 0z"/></svg><span>People</span></button>
      <button type="button" class:active={activeTab==='Quest'} aria-current={activeTab==='Quest'?'page':undefined} onclick={()=>{ensurePlayable();activeTab='Quest';}}><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="5" width="16" height="16" rx="2"/><path d="M8 3v4m8-4v4M4 10h16"/></svg><span>Activity</span></button>
    </nav>
    <main class="studio-main" class:has-inspector={activeTab!=='Room'||detailsOpen}>
      <div class="editor-area">
        <section class="canvas-workspace" class:offstage={activeTab!=='Room'} aria-label="Room preview" aria-hidden={activeTab!=='Room'}>
          <div class="canvas-stage" bind:this={stage}>
            <canvas bind:this={canvas} onpointerdown={pointerDown} onpointermove={pointerMove} onpointerup={pointerUp} onpointercancel={pointerUp} onlostpointercapture={pointerUp} aria-label={`Fictional sample room with ${roomPackage.room.objects.length} pieces. Drag a piece or use its position controls.`}></canvas>
            <div class="room-sign"><span>FICTIONAL ROOM</span><strong>{roomPackage.room.companyName}</strong></div>
            <button class="room-details-trigger" type="button" aria-expanded={detailsOpen} onclick={()=>{detailsOpen=!detailsOpen;moveOpen=false;}}>Room details <span>{detailsOpen?'×':'↗'}</span></button>
            {#if selected}<div class="selection-tools" aria-label="Selected furniture actions"><button type="button" class:tool-active={moveOpen} aria-expanded={moveOpen} onclick={()=>{moveOpen=!moveOpen;detailsOpen=true;}}><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3v18M3 12h18m-4-4 4 4-4 4M8 8l4-4 4 4m-8 8 4 4 4-4"/></svg><span>Move</span></button><button type="button" onclick={duplicateSelected}><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V5a1 1 0 0 0-1-1H5a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h3"/></svg><span>Duplicate</span></button><button type="button" class="remove-action" onclick={deleteSelected}><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16m-10 0V4h4v3m4 0-1 13H7L6 7m3 3v7m6-7v7"/></svg><span>Remove</span></button></div>{/if}
          </div>
          <div class="canvas-footer"><span>Drag to move. Select a piece to edit its position.</span><span><kbd>←</kbd><kbd>↑</kbd><kbd>↓</kbd><kbd>→</kbd> nudge</span></div>
        </section>
        {#if activeTab==='People'}
          <section class="people-preview" aria-label="Character preview"><div class="preview-heading"><span>CHARACTER PREVIEW</span><small>Same appearance in the game</small></div>{#if selectedPerson}<h1>{selectedPerson.displayName}</h1><div class="avatar-preview-stage"><canvas bind:this={portraitCanvas} aria-label={`Pixel character preview of ${selectedPerson.displayName}. Facing ${directions[portraitDirection]}, ${portraitWalking?'walking':'standing still'}.`}></canvas></div><div class="avatar-preview-controls"><div class="facing-controls" role="group" aria-label="Character facing direction">{#each ['Front','Back','Left','Right'] as label,i}<button type="button" aria-pressed={portraitDirection===i} class:active={portraitDirection===i} onclick={()=>{portraitDirection=i;resetPreviewAnimation();drawPortrait();}}>{label}</button>{/each}</div><div class="motion-controls" role="group" aria-label="Character movement preview"><button type="button" aria-pressed={!portraitWalking} class:active={!portraitWalking} onclick={()=>{portraitWalking=false;resetPreviewAnimation();drawPortrait();}}>Stand</button><button type="button" aria-pressed={portraitWalking} class:active={portraitWalking} onclick={()=>{portraitWalking=true;resetPreviewAnimation();drawPortrait();}}>Walk</button></div></div><p class="preview-help">Change the character’s look and details in the People panel.</p>{/if}</section>
        {:else if activeTab==='Quest'}
          <section class="activity-preview" aria-label="Learning activity preview"><div class="preview-heading"><span>ACTIVITY PREVIEW</span><small>Fictional sample</small></div>{#if playable && roomPackage.quest}<article class="activity-card"><p class="activity-kicker">A ROOM CHALLENGE</p><h1>{roomPackage.quest.title}</h1><p>{roomPackage.quest.introduction}</p><div class="clue-cards">{#each roomPackage.quest.clues as clue,i}<section><span>CLUE {i+1}</span><h2>{clue.title}</h2><p>{clue.text}</p></section>{/each}</div><h2 class="question-preview">{roomPackage.quest.question}</h2><div class="answer-preview">{#each roomPackage.quest.answers as answer,i}<div><span>{String.fromCharCode(65+i)}</span>{answer.text}</div>{/each}</div><p class="reward-preview">Reward · {roomPackage.quest.reward.label}</p></article>{/if}</section>
        {/if}
        <p class="status-line" aria-live="polite"><span class="status-dot"></span>{fileStatus||status}</p>
      </div>
      {#if activeTab!=='Room'||detailsOpen}<aside class="inspector" aria-label={activeTab==='Room'?'Room details':activeTab==='People'?'Character details':'Activity details'}>
        {#if activeTab==='Room'}
          <div class="inspector-heading"><span class="eyebrow">ROOM</span><h2>Room details</h2></div>
          <section class="inspector-section"><label class="field-label" for="room-name-side">Room name</label><input id="room-name-side" maxlength="60" value={roomPackage.room.companyName} onchange={e=>{updateRoomName(e.currentTarget.value);e.currentTarget.value=roomPackage.room.companyName;}}/><p class="field-help">A fictional name for this sample space.</p></section>
          {#if selected}<section class="inspector-section selected-section"><div class="section-title"><div><span class="eyebrow">SELECTED PIECE</span><h2>{getProp(selected.kind)?.label}</h2></div></div>{#if moveOpen}<div class="coordinates"><label for="position-x">X position<input id="position-x" type="number" min="0" max="320" step="1" value={selected.x} onchange={e=>{editPosition('x',e.currentTarget.value);e.currentTarget.value=String(roomPackage.room.objects.find(o=>o.id===selectedId)?.x??'');}}/></label><label for="position-y">Y position<input id="position-y" type="number" min="0" max="224" step="1" value={selected.y} onchange={e=>{editPosition('y',e.currentTarget.value);e.currentTarget.value=String(roomPackage.room.objects.find(o=>o.id===selectedId)?.y??'');}}/></label></div><p class="field-help">Room pixels, measured from the upper left.</p><div class="nudge-row"><span>Move 4 pixels</span><div><button type="button" aria-label="Move up 4 pixels" onclick={()=>moveSelected(0,-4)}>↑</button><button type="button" aria-label="Move left 4 pixels" onclick={()=>moveSelected(-4,0)}>←</button><button type="button" aria-label="Move down 4 pixels" onclick={()=>moveSelected(0,4)}>↓</button><button type="button" aria-label="Move right 4 pixels" onclick={()=>moveSelected(4,0)}>→</button></div></div>{:else}<p class="field-help">Choose Move beside the selected piece to edit its position.</p>{/if}</section>{:else}<p class="empty-state">Choose a piece on the room to see its details.</p>{/if}
          <section class="inspector-section room-list-section"><div class="section-title"><div><span class="eyebrow">IN THIS ROOM</span><h2>{roomPackage.room.objects.length} pieces</h2></div></div><div class="object-list" role="group" aria-label="Furniture in the room">{#each roomPackage.room.objects as item (item.id)}<button type="button" class:active={item.id===selectedId} class="object-row" aria-pressed={item.id===selectedId} onclick={()=>{choose(item.id);moveOpen=false;}}><span>{getProp(item.kind)?.label}</span><small>{item.x}, {item.y}</small></button>{:else}<p class="empty-state">Add furniture from the shelf below.</p>{/each}</div></section>
        {:else if activeTab==='People'}
          <div class="inspector-heading"><span class="eyebrow">PEOPLE</span><h2>Character details</h2></div>
          {#if playable}<div class="character-picker">{#each roomPackage.characters as character,i}<button type="button" class:active={selectedCharacter===i} onclick={()=>{selectedCharacter=i;resetPreviewAnimation();}}><span class="avatar-dot">{character.displayName.slice(0,1)}</span><span>{i===0?'Player':'Guide'}<small>{character.displayName}</small></span></button>{/each}</div>{#if selectedPerson}<section class="inspector-section"><label class="field-label" for="person-name">Name</label><input id="person-name" maxlength="40" value={selectedPerson.displayName} onchange={e=>{editText('character','displayName',e.currentTarget.value);e.currentTarget.value=roomPackage.characters[selectedCharacter].displayName;}}/><label class="field-label" for="person-intro">{selectedPersonIsReal?(selectedCharacter===0?'Approved profile introduction':'Approved guide introduction'):(selectedCharacter===0?'Player introduction':'Guide introduction')}</label><textarea id="person-intro" maxlength="160" rows="3" onchange={e=>{editText('character','introduction',e.currentTarget.value);e.currentTarget.value=roomPackage.characters[selectedCharacter].introduction;}}>{selectedPerson.introduction}</textarea><p class="field-help">{selectedPersonIsReal?'Blank until you add an introduction approved by this person.':'Short text shown to a player in the sample game.'}</p><label class="field-label" for="avatar-style">Character look</label><select id="avatar-style" value={isPresetAppearance(selectedPerson.appearance)?selectedPerson.appearance.presetId:'custom'} onchange={e=>chooseAvatarStyle(e.currentTarget.value)}><option value="custom">Custom character</option>{#each Object.entries(avatarPresets) as [presetId,preset]}<option value={presetId}>{preset.label}</option>{/each}</select>{#if isPresetAppearance(selectedPerson.appearance)}<p class="field-help">Choose Custom character to edit clothing and colors.</p>{:else}{#each Object.entries(rules) as [field,options]}<label class="field-label" for={`look-${field}`}>{field.replace(/([A-Z])/g,' $1').replace(/^./,s=>s.toUpperCase())}</label><select id={`look-${field}`} value={selectedPerson.appearance[field as keyof typeof selectedPerson.appearance]} onchange={e=>editAppearance(field,e.currentTarget.value)}>{#each options as option}<option value={option}>{option.replaceAll('-',' ')}</option>{/each}</select>{/each}{/if}</section>{/if}{/if}
        {:else}
          <div class="inspector-heading"><span class="eyebrow">ACTIVITY</span><h2>Activity details</h2></div>
          {#if playable && roomPackage.quest}<section class="inspector-section quest-fields"><label class="field-label" for="quest-title">Activity title</label><input id="quest-title" value={roomPackage.quest.title} maxlength="80" onchange={e=>{editText('quest','title',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.title??'';}}/><label class="field-label" for="quest-objective">What players practice</label><textarea id="quest-objective" rows="2" maxlength="160" onchange={e=>{editText('quest','learningObjective',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.learningObjective??'';}}>{roomPackage.quest.learningObjective}</textarea><label class="field-label" for="quest-intro">Welcome text</label><textarea id="quest-intro" rows="3" maxlength="600" onchange={e=>{editText('quest','introduction',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.introduction??'';}}>{roomPackage.quest.introduction}</textarea>{#each roomPackage.quest.clues as clue,i}<fieldset><legend>Clue {i+1}</legend><label class="field-label" for={`clue-title-${i}`}>Short title</label><input id={`clue-title-${i}`} value={clue.title} maxlength="60" onchange={e=>{editText('clue','title',e.currentTarget.value,i);e.currentTarget.value=roomPackage.quest?.clues[i].title??'';}}/><label class="field-label" for={`clue-text-${i}`}>What players discover</label><textarea id={`clue-text-${i}`} rows="3" maxlength="600" onchange={e=>{editText('clue','text',e.currentTarget.value,i);e.currentTarget.value=roomPackage.quest?.clues[i].text??'';}}>{clue.text}</textarea></fieldset>{/each}<label class="field-label" for="quest-question">Question</label><textarea id="quest-question" rows="2" maxlength="320" onchange={e=>{editText('quest','question',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.question??'';}}>{roomPackage.quest.question}</textarea><fieldset><legend>Three answers</legend>{#each roomPackage.quest.answers as answer,i}<label class="answer-field">Answer {i+1}<input value={answer.text} maxlength="160" onchange={e=>{editText('answer','text',e.currentTarget.value,i);e.currentTarget.value=roomPackage.quest?.answers[i].text??'';}}/><span><input type="radio" name="correct-answer" checked={roomPackage.quest.correctAnswerId===answer.id} onchange={()=>editPackage(d=>{if(d.quest)d.quest.correctAnswerId=answer.id;})}/> Correct answer</span></label>{/each}</fieldset><label class="field-label" for="quest-hint">Hint</label><textarea id="quest-hint" rows="2" maxlength="800" onchange={e=>{editText('quest','hint',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.hint??'';}}>{roomPackage.quest.hint}</textarea><label class="field-label" for="quest-explanation">Answer explanation</label><textarea id="quest-explanation" rows="2" maxlength="800" onchange={e=>{editText('quest','explanation',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.explanation??'';}}>{roomPackage.quest.explanation}</textarea><label class="field-label" for="reward-label">Reward</label><input id="reward-label" value={roomPackage.quest.reward.label} maxlength="80" onchange={e=>{editText('reward','label',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.reward.label??'';}}/></section>
          {#if roomPackage.gameplay}<section class="inspector-section gameplay-section"><span class="eyebrow">ACTIVITY LOCATIONS</span><p class="field-help">Room pixels, in 4-pixel steps.</p>{#each [{label:'Player starts',key:'spawn'},{label:'Guide stands',key:'npc'},...roomPackage.gameplay.clues.map((c,i)=>({label:`Clue ${i+1}`,key:`clue-${i}`}))] as point}<fieldset><legend>{point.label}</legend><div class="coordinates"><label>X<input type="number" step="4" value={gamePoint(point.key).x} onchange={e=>{editGamePoint(point.key,'x',e.currentTarget.value);e.currentTarget.value=String(gamePoint(point.key).x);}}/></label><label>Y<input type="number" step="4" value={gamePoint(point.key).y} onchange={e=>{editGamePoint(point.key,'y',e.currentTarget.value);e.currentTarget.value=String(gamePoint(point.key).y);}}/></label></div></fieldset>{/each}</section>{/if}{/if}
        {/if}
      </aside>{/if}
    </main>
    {#if activeTab==='Room'}<section class="catalog-shelf" aria-label="Furniture catalog"><div class="shelf-top"><div class="shelf-tabs" role="group" aria-label="Furniture categories">{#each categories as category}<button type="button" aria-pressed={selectedCategory===category} class:active={selectedCategory===category} onclick={()=>selectedCategory=category}>{category}</button>{/each}</div><span class="fiction-note">Fictional room · Offline {dirty?'· Unsaved':''}</span></div><div class="shelf-items" aria-live="polite">{#each propsInCategory as prop}<button type="button" class="catalog-card" onclick={()=>add(prop.id)} disabled={roomPackage.room.objects.length>=24}><span class="catalog-thumb" aria-hidden="true"><span class="catalog-thumb-crop" style={thumbnailStyle(prop)}></span></span><span>{prop.label}</span></button>{:else}<p class="empty-state">No furniture in this group yet.</p>{/each}</div></section>{/if}
  </div>
</div>
