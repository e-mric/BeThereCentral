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

  const categories = ['Workspaces', 'Play', 'Wellness', 'Meetings', 'Lounge', 'Secrets'] as const;
  const categoryIcons: Record<string, string> = { Workspaces: '▤', Play: '✳', Wellness: '✿', Meetings: '▣', Lounge: '⌂', Secrets: '✦' };
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
  let selectedCategory = $state<string>('Workspaces');
  let activeTab = $state<'Room' | 'People' | 'Quest'>('Room');
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
  let drag: { id: string; x: number; y: number; px: number; py: number; before: StudioPackage } | null = null;
  const selected = $derived(roomPackage.room.objects.find(item => item.id === selectedId));
  const selectedPerson = $derived(roomPackage.characters[selectedCharacter]);
  const selectedPersonPreset = $derived(selectedPerson && 'presetId' in selectedPerson.appearance ? getAvatarPreset(selectedPerson.appearance.presetId) : null);
  const selectedPersonIsReal = $derived(Boolean(selectedPerson && 'presetId' in selectedPerson.appearance && selectedPersonPreset?.realPerson));
  const propsInCategory = $derived(catalog.filter(prop => prop.category === selectedCategory));
  const playable = $derived(roomPackage.features.length === 2);
  const STORAGE_KEY = 'betherecentral.community-studio.v2';

  $effect(() => {
    activeTab; selectedPerson; portraitDirection; portraitWalking; portraitFrame; avatarDefinition; spriteRevision; walkPoses;
    if (activeTab === 'People' && portraitCanvas && selectedPerson) drawPortrait();
  });

  function markDirty() { dirty = true; status = 'Unsaved changes. Save to keep them on this device.'; }
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
    else status = 'There is no available place for that piece. Try moving another piece first.';
    draw();
  }
  function deleteSelected() {
    if (!selectedId) return;
    const next = removeProp(roomPackage, selectedId);
    if (redrawCommit(next)) selectedId = roomPackage.room.objects.at(-1)?.id ?? null;
  }
  function moveSelected(dx: number, dy: number) {
    if (!selected) return;
    if (!redrawCommit(moveProp(roomPackage, selected.id, selected.x + dx, selected.y + dy))) status = 'That move would place the piece outside the room.';
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
    try { localStorage.setItem(STORAGE_KEY, serializePackage(roomPackage)); dirty = false; status = 'Saved on this device.'; }
    catch { status = 'Could not save. Check this browser’s storage settings.'; }
  }
  function load() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY); if (!raw) { status = 'No saved room is available on this device.'; return; }
      const next = parsePackage(raw); lastCustomCharacter.clear(); roomPackage = next; rememberManualAppearances(next); selectedId = next.room.objects[0]?.id ?? null; undoStack = []; redoStack = []; dirty = false; status = 'Saved room loaded.'; draw();
    } catch (error) { status = `Saved file could not be loaded: ${message(error)}`; }
  }
  function exportFile() {
    const blob = new Blob([serializePackage(roomPackage)], { type: 'application/json' }); const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = 'betherecentral-room-v2.json'; a.click(); URL.revokeObjectURL(url); fileStatus = 'Room file exported.';
  }
  async function importFile(event: Event) {
    const input = event.currentTarget as HTMLInputElement; const file = input.files?.[0]; if (!file) return;
    try {
      if (file.size > MAX_PACKAGE_BYTES) throw new Error(`Files must be ${MAX_PACKAGE_BYTES} bytes or smaller.`);
      const next = parsePackage(await file.text());
      if (redrawCommit(next)) { lastCustomCharacter.clear(); rememberManualAppearances(next); selectedId = next.room.objects[0]?.id ?? null; fileStatus = 'Checked file opened. Your current work can be restored with Undo.'; }
      else fileStatus = 'This file already matches the room on screen.';
    } catch (error) { fileStatus = `File not opened: ${message(error)} Your current room is unchanged.`; }
    input.value = '';
  }
  function message(error: unknown) { return error instanceof Error ? error.message : 'Unexpected file error.'; }
  function ensurePlayable() {
    if (playable) return;
    try {
      const source = makePlayPackage(roomPackage); redrawCommit(source); activeTab = 'People';
    } catch (error) { status = `Could not add the starter activity: ${message(error)}`; }
  }
  function editPackage(change: (draft: StudioPackage) => void) {
    try { redrawCommit(updatePlayable(roomPackage, change)); status = 'Edit checked.'; }
    catch (error) { status = `That edit needs a change: ${message(error)}`; }
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
    if (!redrawCommit(next)) status = 'That position would put part of the piece outside the room.';
  }
  function updateRoomName(value: string) {
    try { redrawCommit(updatePackage(roomPackage, draft => { draft.room.companyName = value.replace(/[\u0000-\u001f\u007f-\u009f]/gu, '').slice(0, 60).trim(); })); }
    catch (error) { status = message(error); }
  }
  function gamePoint(key:string){const g=roomPackage.gameplay!;if(key==='spawn')return g.spawn;if(key==='npc')return g.npc;return g.clues[Number(key.slice(-1))];}
  function editGamePoint(key:string,axis:'x'|'y',raw:string){if(raw.trim()==='')return;const value=Number(raw);if(Number.isInteger(value))editPackage(d=>{if(!d.gameplay)return;const point=key==='spawn'?d.gameplay.spawn:key==='npc'?d.gameplay.npc:d.gameplay.clues[Number(key.slice(-1))];point[axis]=value;});}

  function insidePolygon(x: number, y: number, polygon: number[][]) {
    let inside = false;
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i, i += 1) { const [xi,yi]=polygon[i], [xj,yj]=polygon[j]; if ((yi>y)!==(yj>y) && x < ((xj-xi)*(y-yi))/(yj-yi)+xi) inside=!inside; }
    return inside;
  }
  function roomTransform() {
    const rect = stage.getBoundingClientRect(); const scale = Math.min((rect.width - 50) / 320, (rect.height - 70) / 224);
    return { scale, left:(rect.width-320*scale)/2, top:(rect.height-224*scale)/2+10 };
  }
  function draw() {
    if (!canvas || !stage) return;
    const rect = stage.getBoundingClientRect(); const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const w = Math.max(1,Math.floor(rect.width*dpr)), h=Math.max(1,Math.floor(rect.height*dpr));
    if (canvas.width!==w || canvas.height!==h) { canvas.width=w; canvas.height=h; }
    const ctx=canvas.getContext('2d'); if(!ctx) return; ctx.clearRect(0,0,w,h); ctx.fillStyle='#111719';ctx.fillRect(0,0,w,h);
    const {scale,left,top}=roomTransform(); const s=scale*dpr; ctx.save();ctx.translate(left*dpr,top*dpr);ctx.scale(s,s);
    const polygon=roomPackage.room.room.polygon;ctx.beginPath();polygon.forEach(([x,y],i)=>i?ctx.lineTo(x,y):ctx.moveTo(x,y));ctx.closePath();ctx.fillStyle='#946b50';ctx.fill();ctx.save();ctx.clip();
    ctx.fillStyle='rgba(82,56,43,.42)';for(let y=8;y<224;y+=8)ctx.fillRect(0,y,320,1);for(let y=0;y<224;y+=8)for(let x=32;x<320;x+=32)if(insidePolygon(x,y+4,polygon))ctx.fillRect(x,y,1,8);
    const g=playable ? roomPackage.gameplay : null;
    const actors=g?[{id:g.playerCharacterId,x:g.spawn.x,y:g.spawn.y},{id:g.npc.characterId,x:g.npc.x,y:g.npc.y}]:[];
    if(g)g.clues.forEach((c,i)=>drawMarker(ctx,c.x,c.y,String(i+1),'#ed9160'));
    const ordered=paintElements(roomPackage.room.objects,actors);
    for(const element of ordered){
      if(element.type==='prop')drawObject(ctx,element.item);
      else { const character=roomPackage.characters.find(item=>item.id===element.actor.id);if(character)drawAvatar(ctx,character,element.actor.x,element.actor.y,element.actor.id===g?.playerCharacterId?'front':'left','idle'); }
    }
    for(const element of ordered)if(element.type==='character'){const character=roomPackage.characters.find(item=>item.id===element.actor.id);if(character)drawCharacterLabel(ctx,character,element.actor.x,element.actor.y);}
    ctx.restore();ctx.beginPath();polygon.forEach(([x,y],i)=>i?ctx.lineTo(x,y):ctx.moveTo(x,y));ctx.closePath();ctx.strokeStyle='#665448';ctx.lineWidth=5;ctx.stroke();
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
    if(item.id===selectedId){ctx.strokeStyle='#df8e4e';ctx.lineWidth=2;ctx.setLineDash([3,2]);ctx.strokeRect(item.x-2,item.y-2,item.width+4,item.height+4);ctx.setLineDash([]);}
  }
  function assetUrl(path:string,sha256:string){return `/assets/community/${path}?v=${encodeURIComponent(sha256)}`;}
  function thumbnailStyle(prop: (typeof catalog)[number]) { spriteRevision;const [x,y,w,h]=prop.crop;const factor=Math.min(48/w,48/h);const [iw,ih]=canvasImageSize(prop.image);return `background-image:url('${assetUrl(prop.image,prop.sha256)}');background-size:${iw*factor}px ${ih*factor}px;background-position:-${x*factor}px -${y*factor}px`; }
  function canvasImageSize(path:string):[number,number]{const img=sprites.get(path);return [img?.naturalWidth||1254,img?.naturalHeight||1254];}
  function pointerPoint(event:PointerEvent){const r=stage.getBoundingClientRect(),t=roomTransform();return{x:(event.clientX-r.left-t.left)/t.scale,y:(event.clientY-r.top-t.top)/t.scale};}
  function pointerDown(event:PointerEvent){const p=pointerPoint(event);const item=hitTestObject(roomPackage.room.objects,p.x,p.y);if(!item){selectedId=null;draw();return;}choose(item.id);drag={id:item.id,x:item.x,y:item.y,px:p.x,py:p.y,before:clonePackage(roomPackage)};canvas.setPointerCapture(event.pointerId);}
  function pointerMove(event:PointerEvent){if(!drag)return;const p=pointerPoint(event);const next=moveProp(roomPackage,drag.id,drag.x+p.x-drag.px,drag.y+p.y-drag.py);if(next){roomPackage=next;markDirty();draw();}}
  function pointerUp(){if(!drag)return;if(JSON.stringify(drag.before)!==JSON.stringify(roomPackage)){undoStack=[...undoStack.slice(-79),drag.before];redoStack=[];}drag=null;}
  function keydown(event:KeyboardEvent){if(['INPUT','TEXTAREA','SELECT'].includes((event.target as HTMLElement).tagName)||(event.target as HTMLElement).isContentEditable)return;if((event.metaKey||event.ctrlKey)&&event.key.toLowerCase()==='z'){event.preventDefault();history(event.shiftKey?'redo':'undo');return;}const d:Record<string,[number,number]>={ArrowUp:[0,-4],ArrowDown:[0,4],ArrowLeft:[-4,0],ArrowRight:[4,0]};if(d[event.key]){event.preventDefault();moveSelected(...d[event.key]);}if(event.key==='Delete')deleteSelected();}
  onMount(()=>{const paths=[...new Set(catalog.map(p=>p.image))];for(const path of paths){const prop=catalog.find(item=>item.image===path)!;const image=new Image();image.onload=()=>{spriteRevision+=1;draw();};image.src=assetUrl(path,prop.sha256);sprites.set(path,image);}const observer=new ResizeObserver(()=>{draw();drawPortrait();});observer.observe(stage);let timer=0;const avatarLoads=[fetch('/assets/community/avatar-parts-v1.json').then(response=>{if(!response.ok)throw new Error('Shared character artwork could not be loaded.');return response.json();}).then(value=>{avatarDefinition=value as AvatarDefinition;}),...Object.entries(avatarPresets).map(async([id,preset])=>{const response=await fetch(assetUrl(preset.image,preset.sha256));if(!response.ok)throw new Error(`${preset.label} artwork could not be loaded.`);const decoded=await decodePngRgba(new Uint8Array(await response.arrayBuffer()));avatarAtlases.set(id,decoded);})];void Promise.all(avatarLoads).then(()=>{spriteRevision+=1;timer=window.setInterval(()=>{const now=performance.now();if(activeTab!=='People'||!portraitWalking||!selectedPerson){lastWalkFrameAt=now;return;}const preset=isPresetAppearance(selectedPerson.appearance)?getAvatarPreset(selectedPerson.appearance.presetId):null;const duration=avatarWalkFrameDuration(preset,directions[portraitDirection],avatarDefinition?.frameDurationMs??160);const advanced=advanceAvatarWalkClock(portraitFrame,walkElapsedMs+Math.max(0,now-lastWalkFrameAt),duration,walkPoses.length);lastWalkFrameAt=now;walkElapsedMs=advanced.elapsedMs;if(portraitFrame!==advanced.index){portraitFrame=advanced.index;drawPortrait();}},40);draw();drawPortrait();}).catch(error=>{status=message(error);});draw();window.addEventListener('keydown',keydown);window.addEventListener('resize',drawPortrait);return()=>{observer.disconnect();window.removeEventListener('keydown',keydown);window.removeEventListener('resize',drawPortrait);if(timer)window.clearInterval(timer);for(const image of sprites.values())image.removeAttribute('src');sprites.clear();avatarAtlases.clear();avatarFrames.clear();};});
</script>

<svelte:head><title>Community Studio · BeThereCentral</title><meta name="description" content="Arrange a sample room and make a small guided learning activity." /></svelte:head>
<div class="app-shell">
  <header class="topbar"><a class="brand" href="/" aria-label="BeThereCentral Studio home"><span class="brand-mark">B</span><span>BeThereCentral<small>COMMUNITY STUDIO</small></span></a><div class="topbar-right"><span class="prototype-tag"><i></i> LOCAL EDITOR</span><span class="version-chip">{dirty ? 'UNSAVED' : 'READY'}</span></div></header>
  <main>
    <section class="page-heading"><div><p class="eyebrow">CREATE <span>/</span> FICTIONAL SAMPLE</p><h1>Shape a shared space.</h1><p class="subhead">Choose room pieces, then add people and a short learning activity.</p></div><div class="journey-tag"><span>1</span> ROOM <b>→</b> <span>2</span> PEOPLE <b>→</b> <span>3</span> ACTIVITY</div></section>
    <nav class="editor-tabs" aria-label="What you are creating"><button class:active={activeTab==='Room'} onclick={()=>activeTab='Room'}>Room</button><button class:active={activeTab==='People'} onclick={()=>{ensurePlayable();activeTab='People';}}>People</button><button class:active={activeTab==='Quest'} onclick={()=>{ensurePlayable();activeTab='Quest';}}>Learning activity</button></nav>
    <div class="studio-layout">
      <aside class="asset-browser" aria-label="Room-piece catalog">
        <div class="pane-heading"><div><p class="eyebrow">THE CATALOG</p><h2>Room pieces</h2></div><span class="count">{roomPackage.room.objects.length} / 24</span></div><p class="pane-hint">Pick a piece to add it to your room.</p>
        <nav class="category-list" aria-label="Furniture categories">{#each categories as category}<button type="button" class:current={selectedCategory===category} aria-pressed={selectedCategory===category} onclick={()=>selectedCategory=category}><span>{categoryIcons[category]}</span><span>{category}</span><small>{catalog.filter(p=>p.category===category).length}</small></button>{/each}</nav>
        <div class="catalog-items" aria-live="polite">{#each propsInCategory as prop}<button type="button" class="catalog-card" onclick={()=>add(prop.id)} disabled={roomPackage.room.objects.length>=24}><span class="catalog-thumb" style={thumbnailStyle(prop)} aria-hidden="true"></span><span class="catalog-card-copy"><strong>{prop.label}</strong><small>{prop.interaction ? 'Special room piece' : prop.category}</small></span><span class="add-mark">+</span></button>{:else}<div class="catalog-empty"><span class="empty-art">{categoryIcons[selectedCategory]}</span><strong>Nothing in this group yet.</strong><p>Choose another room-piece group.</p></div>{/each}</div>
        <p class="catalog-note">Original sample artwork. Every piece belongs to a fictional space.</p>
      </aside>
      <section class="canvas-workspace" aria-label="Room preview">
        <div class="canvas-heading"><div><p class="eyebrow">YOUR SPACE</p><h2>{roomPackage.room.companyName}</h2></div><span class="sample-badge"><i></i> FICTIONAL SAMPLE</span></div>
        <div class="canvas-frame"><div class="canvas-stage" bind:this={stage}><canvas bind:this={canvas} onpointerdown={pointerDown} onpointermove={pointerMove} onpointerup={pointerUp} onpointercancel={pointerUp} onlostpointercapture={pointerUp} aria-label={`Fictional sample room with ${roomPackage.room.objects.length} pieces. Use the list and number fields to edit.`}></canvas><div class="room-sign"><span>FICTIONAL SPACE</span><strong>{roomPackage.room.companyName}</strong></div><div class="scene-label"><span>320 × 224 px</span><b>top-down view</b></div></div><div class="canvas-footer"><span>Drag a piece to move it. Use the number fields for exact positions.</span><span><kbd>←</kbd><kbd>↑</kbd><kbd>↓</kbd><kbd>→</kbd> move</span></div></div>
        {#if activeTab==='People' && selectedPerson}<section class="avatar-preview" aria-label="Live character preview"><div class="avatar-preview-heading"><div><p class="eyebrow">LIVE CHARACTER PREVIEW</p><h3>{selectedPerson.displayName}</h3></div><span>Same look in the game</span></div><div class="avatar-preview-stage"><canvas bind:this={portraitCanvas} aria-label={`Pixel character preview of ${selectedPerson.displayName}. Facing ${['front','back','left','right'][portraitDirection]}, ${portraitWalking?'walking':'standing still'}.`}></canvas></div><div class="avatar-preview-controls"><div class="facing-controls" role="group" aria-label="Character facing direction">{#each ['Front','Back','Left','Right'] as label,i}<button type="button" aria-pressed={portraitDirection===i} class:active={portraitDirection===i} onclick={()=>{portraitDirection=i;resetPreviewAnimation();drawPortrait();}}>{label}</button>{/each}</div><div class="motion-controls" role="group" aria-label="Character movement preview"><button type="button" aria-pressed={!portraitWalking} class:active={!portraitWalking} onclick={()=>{portraitWalking=false;resetPreviewAnimation();drawPortrait();}}>Stand</button><button type="button" aria-pressed={portraitWalking} class:active={portraitWalking} onclick={()=>{portraitWalking=true;resetPreviewAnimation();drawPortrait();}}>Walk</button></div></div><p class="field-help">Front, back, and side views use the game’s pixel-art style. Walking cycles through the available animation frames.</p></section>{/if}
        <div class="canvas-toolbar"><div class="selection-summary"><span class="tool-icon">⌖</span><span>{#if selected}<strong>{getProp(selected.kind)?.label} selected</strong><small>Position {selected.x}, {selected.y}</small>{:else}<strong>Nothing selected</strong><small>Choose a piece on the room preview</small>{/if}</span></div><div class="toolbar-actions"><button type="button" disabled={!undoStack.length} onclick={()=>history('undo')}>↶ <span>Undo</span></button><button type="button" disabled={!redoStack.length} onclick={()=>history('redo')}>↷ <span>Redo</span></button><span class="toolbar-divider"></span><span class="room-object-count">{roomPackage.room.objects.length} pieces</span></div></div><p class="canvas-status" aria-live="polite">{dirty?'● ':''}{status}</p>
      </section>
      <aside class="inspector" aria-label="Selected item and package details">
        {#if activeTab==='Room'}
          <div class="pane-heading"><div><p class="eyebrow">ROOM DETAILS</p><h2>Make it yours</h2></div></div>
          <section class="inspector-section"><div class="section-label"><span class="section-icon">⌂</span><div><strong>Room sign</strong><small>Shown above the preview</small></div></div><label class="field-label" for="company-name">Name this space <span>fictional</span></label><input id="company-name" value={roomPackage.room.companyName} maxlength="60" onchange={e=>{updateRoomName(e.currentTarget.value);e.currentTarget.value=roomPackage.room.companyName;}}/><p class="field-help">Use a made-up name. This sample is not linked to a real company.</p></section>
          <section class="inspector-section selected-section"><div class="section-label"><span class="section-icon">✳</span><div><strong>Selected piece</strong><small>{selected?getProp(selected.kind)?.label:'Choose an item first'}</small></div></div>{#if selected}<div class="coordinates"><div><label for="position-x">X <input id="position-x" type="number" min="0" max="320" step="1" value={selected.x} onchange={e=>{editPosition('x',e.currentTarget.value);e.currentTarget.value=String(roomPackage.room.objects.find(o=>o.id===selectedId)?.x??'');}}/></label><label for="position-y">Y <input id="position-y" type="number" min="0" max="224" step="1" value={selected.y} onchange={e=>{editPosition('y',e.currentTarget.value);e.currentTarget.value=String(roomPackage.room.objects.find(o=>o.id===selectedId)?.y??'');}}/></label></div><small>Top-left pixels · right and down</small></div><div class="nudge-row"><span>Move a little</span><div><button type="button" aria-label="Move up 4 pixels" onclick={()=>moveSelected(0,-4)}>↑</button><button type="button" aria-label="Move left 4 pixels" onclick={()=>moveSelected(-4,0)}>←</button><button type="button" aria-label="Move down 4 pixels" onclick={()=>moveSelected(0,4)}>↓</button><button type="button" aria-label="Move right 4 pixels" onclick={()=>moveSelected(4,0)}>→</button></div></div><button type="button" class="remove-button" onclick={deleteSelected}>Remove this piece <span>⌫</span></button>{:else}<p class="selection-help">Select a piece in the room to change its position.</p>{/if}</section>
          <section class="inspector-section layers-section"><div class="section-label"><span class="section-icon">▤</span><div><strong>In this room</strong><small>{roomPackage.room.objects.length} pieces</small></div></div><div class="object-list" role="listbox" aria-label="Pieces in the room">{#each roomPackage.room.objects as item (item.id)}<button type="button" class:active={item.id===selectedId} class="object-row" role="option" aria-selected={item.id===selectedId} onclick={()=>choose(item.id)}><span class="mini-furniture">✳</span><span>{getProp(item.kind)?.label}</span><small>{item.x}, {item.y}</small></button>{:else}<p class="empty-list">No pieces yet. Choose one from the catalog.</p>{/each}</div></section>
        {:else if activeTab==='People'}
          <div class="pane-heading"><div><p class="eyebrow">TWO CHARACTERS</p><h2>People in the room</h2></div></div>{#if !playable}<button class="primary-button" onclick={ensurePlayable}>Add a player and guide</button>{:else}<div class="character-picker">{#each roomPackage.characters as character,i}<button type="button" class:active={selectedCharacter===i} onclick={()=>{selectedCharacter=i;resetPreviewAnimation();}}><span class="avatar-dot">{character.displayName.slice(0,1)}</span><span>{i===0?'Player':'Guide'}<small>{character.displayName}</small></span></button>{/each}</div>{#if selectedPerson}<section class="inspector-section"><label class="field-label" for="person-name">Name</label><input id="person-name" maxlength="40" value={selectedPerson.displayName} onchange={e=>{editText('character','displayName',e.currentTarget.value);e.currentTarget.value=roomPackage.characters[selectedCharacter].displayName;}}/><label class="field-label" for="person-intro">{selectedPersonIsReal?(selectedCharacter===0?'Approved profile introduction':'Approved guide introduction'):(selectedCharacter===0?'Player profile introduction':'Guide greeting spoken when approached')}</label><textarea id="person-intro" maxlength="160" rows="3" onchange={e=>{editText('character','introduction',e.currentTarget.value);e.currentTarget.value=roomPackage.characters[selectedCharacter].introduction;}}>{selectedPerson.introduction}</textarea><p class="field-help">{selectedPersonIsReal?'Blank until you add an introduction approved by this person.':selectedCharacter===0?'A short profile for the person playing.':'The guide says this when a player comes close.'}</p><label class="field-label" for="avatar-style">Character look</label><select id="avatar-style" value={isPresetAppearance(selectedPerson.appearance)?selectedPerson.appearance.presetId:'custom'} onchange={e=>chooseAvatarStyle(e.currentTarget.value)}><option value="custom">Custom character</option>{#each Object.entries(avatarPresets) as [presetId,preset]}<option value={presetId}>{preset.label}</option>{/each}</select>{#if isPresetAppearance(selectedPerson.appearance)}<p class="field-help">{avatarPresets[selectedPerson.appearance.presetId]?.label ?? 'This preset'} is a fixed look. Choose Custom character to edit clothing and colors.</p>{:else}{#each Object.entries(rules) as [field,options]}<label class="field-label" for={`look-${field}`}>{field.replace(/([A-Z])/g,' $1').replace(/^./,s=>s.toUpperCase())}</label><select id={`look-${field}`} value={selectedPerson.appearance[field as keyof typeof selectedPerson.appearance]} onchange={e=>editAppearance(field,e.currentTarget.value)}>{#each options as option}<option value={option}>{option.replaceAll('-',' ')}</option>{/each}</select>{/each}{/if}</section>{/if}{/if}
        {:else}
          <div class="pane-heading"><div><p class="eyebrow">A SHORT ACTIVITY</p><h2>Learning activity</h2></div></div>{#if !playable}<button class="primary-button" onclick={ensurePlayable}>Add the starter activity</button>{:else if roomPackage.quest}<section class="inspector-section quest-fields"><label class="field-label" for="quest-title">Activity title</label><input id="quest-title" value={roomPackage.quest.title} maxlength="80" onchange={e=>{editText('quest','title',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.title??'';}}/><label class="field-label" for="quest-objective">What players practice</label><textarea id="quest-objective" rows="2" maxlength="160" onchange={e=>{editText('quest','learningObjective',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.learningObjective??'';}}>{roomPackage.quest.learningObjective}</textarea><label class="field-label" for="quest-intro">Welcome text</label><textarea id="quest-intro" rows="3" maxlength="600" onchange={e=>{editText('quest','introduction',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.introduction??'';}}>{roomPackage.quest.introduction}</textarea>{#each roomPackage.quest.clues as clue,i}<fieldset><legend>Clue {i+1}</legend><label class="field-label" for={`clue-title-${i}`}>Short title</label><input id={`clue-title-${i}`} value={clue.title} maxlength="60" onchange={e=>{editText('clue','title',e.currentTarget.value,i);e.currentTarget.value=roomPackage.quest?.clues[i].title??'';}}/><label class="field-label" for={`clue-text-${i}`}>What players discover</label><textarea id={`clue-text-${i}`} rows="3" maxlength="600" onchange={e=>{editText('clue','text',e.currentTarget.value,i);e.currentTarget.value=roomPackage.quest?.clues[i].text??'';}}>{clue.text}</textarea></fieldset>{/each}<label class="field-label" for="quest-question">Question</label><textarea id="quest-question" rows="2" maxlength="320" onchange={e=>{editText('quest','question',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.question??'';}}>{roomPackage.quest.question}</textarea><fieldset><legend>Three answers</legend>{#each roomPackage.quest.answers as answer,i}<label class="answer-field">Answer {i+1}<input value={answer.text} maxlength="160" onchange={e=>{editText('answer','text',e.currentTarget.value,i);e.currentTarget.value=roomPackage.quest?.answers[i].text??'';}}/><span><input type="radio" name="correct-answer" checked={roomPackage.quest.correctAnswerId===answer.id} onchange={()=>editPackage(d=>{if(d.quest)d.quest.correctAnswerId=answer.id;})}/> Correct answer</span></label>{/each}</fieldset><label class="field-label" for="quest-hint">Hint</label><textarea id="quest-hint" rows="2" maxlength="800" onchange={e=>{editText('quest','hint',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.hint??'';}}>{roomPackage.quest.hint}</textarea><label class="field-label" for="quest-explanation">Answer explanation</label><textarea id="quest-explanation" rows="2" maxlength="800" onchange={e=>{editText('quest','explanation',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.explanation??'';}}>{roomPackage.quest.explanation}</textarea><label class="field-label" for="reward-label">Reward</label><input id="reward-label" value={roomPackage.quest.reward.label} maxlength="80" onchange={e=>{editText('reward','label',e.currentTarget.value);e.currentTarget.value=roomPackage.quest?.reward.label??'';}}/></section>{/if}
        {/if}
        {#if activeTab==='Quest' && playable && roomPackage.gameplay}<section class="inspector-section gameplay-section"><div class="section-label"><span class="section-icon">⌖</span><div><strong>Places to visit</strong><small>Numbers are room pixels; 4-pixel steps</small></div></div>{#each [{label:'Player starts',key:'spawn'},{label:'Guide stands',key:'npc'},...roomPackage.gameplay.clues.map((c,i)=>({label:`Clue ${i+1}`,key:`clue-${i}`}))] as point}<fieldset><legend>{point.label}</legend><div class="coordinates"><label>X<input type="number" step="4" value={gamePoint(point.key).x} onchange={e=>{editGamePoint(point.key,'x',e.currentTarget.value);e.currentTarget.value=String(gamePoint(point.key).x);}}/></label><label>Y<input type="number" step="4" value={gamePoint(point.key).y} onchange={e=>{editGamePoint(point.key,'y',e.currentTarget.value);e.currentTarget.value=String(gamePoint(point.key).y);}}/></label></div></fieldset>{/each}</section>{/if}
        <section class="inspector-section save-section"><div class="section-label"><span class="section-icon save-icon">⌂</span><div><strong>Keep a copy</strong><small>Only stored when you choose</small></div></div><div class="local-actions"><button class="primary-button" type="button" onclick={save}>Save on this device</button><button class="secondary-button" type="button" onclick={load}>Load saved work</button></div><div class="file-actions"><button type="button" onclick={exportFile}>Export file <span>↓</span></button><label for="package-file" class="import-button">Import file <span>↑</span></label><input id="package-file" class="visually-hidden" type="file" accept="application/json,.json" onchange={importFile}/></div><p class="file-status" aria-live="polite">{fileStatus||'Checked room files can be moved to another device.'}</p></section>
      </aside>
    </div>
    <p class="fiction-note"><span>✦</span><strong>Fictional sample space.</strong> Its shape and label do not represent a real tenant. Files are checked before they replace work on screen.</p>
  </main>
  <footer class="footer-note"><span>BE THERE, TOGETHER</span><b>·</b> Local browser editor <b>·</b> Checked room package v2</footer>
</div>
