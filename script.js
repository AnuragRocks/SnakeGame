(function(){
  const canvas = document.getElementById('canvas');
  const ctx = canvas.getContext('2d');
  const scoreEl = document.getElementById('score');
  const restartBtn = document.getElementById('restart');
  const fullscreenBtn = document.getElementById('fullscreen');

  let dpr = Math.max(1, window.devicePixelRatio || 1);
  let grid = 24; // base tile size (CSS pixels)
  let cols = 20, rows = 20;
  let snake = [{x:5,y:5}];
  let dir = {x:1,y:0};
  let apple = {x:10,y:10};
  let speed = 6; // steps per second
  let lastStep = 0;
  let running = true;

  function resizeCanvas(){
    // size the canvas to fit available viewport while keeping square
    const maxW = Math.min(window.innerWidth - 40, 1200);
    const maxH = Math.min(window.innerHeight - 140, 900);
    const size = Math.max(240, Math.min(maxW, maxH));
    canvas.style.width = size + 'px';
    canvas.style.height = size + 'px';
    canvas.width = Math.round(size * dpr);
    canvas.height = Math.round(size * dpr);
    ctx.setTransform(dpr,0,0,dpr,0,0);

    // compute grid (more cols on larger screens)
    cols = Math.max(10, Math.floor(size / grid));
    rows = cols;
  }

  window.addEventListener('resize', ()=>{ dpr = Math.max(1, window.devicePixelRatio || 1); resizeCanvas(); });
  resizeCanvas();

  function placeApple(){
    apple.x = Math.floor(Math.random()*cols);
    apple.y = Math.floor(Math.random()*rows);
    if(snake.some(p=>p.x===apple.x && p.y===apple.y)) placeApple();
  }

  function step(now){
    if(!lastStep) lastStep = now;
    const elapsed = now - lastStep;
    const stepMs = 1000 / Math.max(1, speed);
    if(elapsed >= stepMs){
      if(running) update();
      lastStep = now - (elapsed % stepMs);
    }
    draw();
    requestAnimationFrame(step);
  }

  function update(){
    const head = {x:snake[0].x + dir.x, y:snake[0].y + dir.y};
    head.x = (head.x + cols) % cols;
    head.y = (head.y + rows) % rows;
    if(snake.some(p=>p.x===head.x && p.y===head.y)) { running=false; return; }
    snake.unshift(head);
    if(head.x===apple.x && head.y===apple.y){ placeApple(); speed = Math.min(30, speed + 0.4); }
    else snake.pop();
    scoreEl.textContent = 'Score: ' + (snake.length-1);
  }

  function draw(){
    const cssW = parseInt(canvas.style.width,10);
    const cssH = parseInt(canvas.style.height,10);
    ctx.clearRect(0,0,cssW,cssH);
    // background
    const g = ctx.createLinearGradient(0,0,0,cssH);
    g.addColorStop(0,'#062a36'); g.addColorStop(1,'#01384a'); ctx.fillStyle = g; ctx.fillRect(0,0,cssW,cssH);

    const cell = cssW / cols;

    // draw apple
    ctx.fillStyle = '#e74c3c'; ctx.beginPath(); ctx.arc((apple.x+0.5)*cell, (apple.y+0.5)*cell, cell*0.38, 0, Math.PI*2); ctx.fill();

    // draw snake
    for(let i=0;i<snake.length;i++){
      const p = snake[i]; ctx.fillStyle = i===0 ? '#1abc9c' : '#2ecc71';
      roundRect(ctx, p.x*cell + cell*0.06, p.y*cell + cell*0.06, cell - cell*0.12, cell - cell*0.12, Math.max(6,cell*0.15), true, false);
    }

    if(!running){ ctx.fillStyle='rgba(0,0,0,0.5)'; ctx.fillRect(0,cssH/2-48,cssW,96); ctx.fillStyle='#fff'; ctx.font='24px sans-serif'; ctx.textAlign='center'; ctx.fillText('Game Over — Restart to play again', cssW/2, cssH/2+8); }
  }

  function roundRect(ctx,x,y,w,h,r,fill,stroke){ if(typeof r==='undefined') r=5; ctx.beginPath(); ctx.moveTo(x+r,y); ctx.arcTo(x+w,y,x+w,y+h,r); ctx.arcTo(x+w,y+h,x,y+h,r); ctx.arcTo(x,y+h,x,y,r); ctx.arcTo(x,y,x+w,y,r); ctx.closePath(); if(fill) ctx.fill(); if(stroke) ctx.stroke(); }

  // input handlers
  document.addEventListener('keydown', e=>{
    if(!running && (e.key==='r' || e.key==='R')) { restart(); return; }
    if(e.key==='ArrowUp' || e.key==='w' || e.key==='W'){ if(dir.y===0) dir={x:0,y:-1}; }
    if(e.key==='ArrowDown' || e.key==='s' || e.key==='S'){ if(dir.y===0) dir={x:0,y:1}; }
    if(e.key==='ArrowLeft' || e.key==='a' || e.key==='A'){ if(dir.x===0) dir={x:-1,y:0}; }
    if(e.key==='ArrowRight' || e.key==='d' || e.key==='D'){ if(dir.x===0) dir={x:1,y:0}; }
  });

  // touch controls
  const upBtn = document.getElementById('up'), downBtn = document.getElementById('down');
  const leftBtn = document.getElementById('left'), rightBtn = document.getElementById('right');
  function touchDir(dx,dy){ if(dx!==0 && dir.x===0) dir={x:dx,y:0}; if(dy!==0 && dir.y===0) dir={x:0,y:dy}; }
  ['touchstart','mousedown'].forEach(ev=>{ upBtn.addEventListener(ev, ()=>touchDir(0,-1)); downBtn.addEventListener(ev, ()=>touchDir(0,1)); leftBtn.addEventListener(ev, ()=>touchDir(-1,0)); rightBtn.addEventListener(ev, ()=>touchDir(1,0)); });

  // fullscreen
  fullscreenBtn.addEventListener('click', ()=>{
    const el = document.documentElement;
    if(!document.fullscreenElement) el.requestFullscreen().catch(()=>{});
    else document.exitFullscreen();
  });

  // gamepad support (basic)
  let gpIndex = null;
  window.addEventListener('gamepadconnected', e=>{ gpIndex = e.gamepad.index; console.log('gamepad connected', e.gamepad.id); });
  window.addEventListener('gamepaddisconnected', e=>{ if(gpIndex===e.gamepad.index) gpIndex=null; });
  function pollGamepad(){ if(gpIndex===null) return; const g = navigator.getGamepads()[gpIndex]; if(!g) return; // map dpad / axes
    if(g.buttons[12] && g.buttons[12].pressed) { if(dir.y===0) dir={x:0,y:-1}; }
    if(g.buttons[13] && g.buttons[13].pressed) { if(dir.y===0) dir={x:0,y:1}; }
    if(g.buttons[14] && g.buttons[14].pressed) { if(dir.x===0) dir={x:-1,y:0}; }
    if(g.buttons[15] && g.buttons[15].pressed) { if(dir.x===0) dir={x:1,y:0}; }
  }

  restartBtn.addEventListener('click', restart);

  function restart(){ snake = [{x:Math.floor(cols/2), y:Math.floor(rows/2)}]; dir={x:1,y:0}; placeApple(); speed=6; running=true; scoreEl.textContent='Score: 0'; }

  // start
  placeApple(); requestAnimationFrame(step);
  setInterval(pollGamepad, 100);

})();
(() => {
  const canvas = document.getElementById('canvas');
  const ctx = canvas.getContext('2d');
  const scoreEl = document.getElementById('score');
  const restartBtn = document.getElementById('restart');

  let grid = 20; // tile size
  let cols, rows;
  let snake = [{x:5,y:5}];
  let dir = {x:1,y:0};
  let apple = {x:10,y:10};
  let speed = 6; // steps per second
  let frames = 0;
  let running = true;

  function fit() {
    const min = Math.min(window.innerWidth, window.innerHeight) - 40;
    const size = Math.max(200, Math.min(600, min));
    canvas.width = canvas.height = size;
    cols = Math.floor(size / grid);
    rows = Math.floor(size / grid);
  }
  window.addEventListener('resize', fit);
  fit();

  function placeApple(){
    apple.x = Math.floor(Math.random()*cols);
    apple.y = Math.floor(Math.random()*rows);
    // avoid placing on snake
    if(snake.some(p=>p.x===apple.x && p.y===apple.y)) placeApple();
  }

  function loop(){
    frames++;
    const interval = Math.max(1, Math.floor(60 / speed));
    if(frames % interval === 0 && running){
      update();
    }
    draw();
    requestAnimationFrame(loop);
  }

  function update(){
    const head = {x:snake[0].x + dir.x, y:snake[0].y + dir.y};
    // wrap around
    head.x = (head.x + cols) % cols;
    head.y = (head.y + rows) % rows;

    // collision with body
    if(snake.some(p=>p.x===head.x && p.y===head.y)){
      running = false; return;
    }

    snake.unshift(head);
    if(head.x===apple.x && head.y===apple.y){
      placeApple();
      speed += 0.3; // increase speed gradually
    } else {
      snake.pop();
    }
    scoreEl.textContent = 'Score: ' + (snake.length - 1);
  }

  function draw(){
    ctx.clearRect(0,0,canvas.width,canvas.height);
    const w = canvas.width, h = canvas.height;
    // background
    const g = ctx.createLinearGradient(0,0,0,h);
    g.addColorStop(0,'#062a36'); g.addColorStop(1,'#01384a');
    ctx.fillStyle = g; ctx.fillRect(0,0,w,h);

    // apple
    ctx.fillStyle = '#e74c3c';
    ctx.beginPath(); ctx.arc((apple.x+0.5)*grid, (apple.y+0.5)*grid, grid*0.4,0,Math.PI*2); ctx.fill();

    // snake
    for(let i=0;i<snake.length;i++){
      const p = snake[i];
      ctx.fillStyle = i===0 ? '#1abc9c' : '#2ecc71';
      roundRect(ctx, p.x*grid + 2, p.y*grid + 2, grid-4, grid-4, 6, true, false);
    }

    if(!running){
      ctx.fillStyle = 'rgba(0,0,0,0.5)'; ctx.fillRect(0,h/2-40,w,80);
      ctx.fillStyle = '#fff'; ctx.font = '24px sans-serif'; ctx.textAlign='center';
      ctx.fillText('Game Over — Tap Restart', w/2, h/2+8);
    }
  }

  function roundRect(ctx,x,y,w,h,r,fill,stroke){
    if(typeof r==='undefined') r=5; ctx.beginPath(); ctx.moveTo(x+r,y);
    ctx.arcTo(x+w,y,x+w,y+h,r); ctx.arcTo(x+w,y+h,x,y+h,r); ctx.arcTo(x,y+h,x,y,r); ctx.arcTo(x,y,x+w,y,r);
    ctx.closePath(); if(fill) ctx.fill(); if(stroke) ctx.stroke();
  }

  document.addEventListener('keydown', e=>{
    if(!running && e.key==='r'){ restart(); return }
    if(e.key==='ArrowUp' || e.key==='w') { if(dir.y===0) dir={x:0,y:-1} }
    if(e.key==='ArrowDown' || e.key==='s') { if(dir.y===0) dir={x:0,y:1} }
    if(e.key==='ArrowLeft' || e.key==='a') { if(dir.x===0) dir={x:-1,y:0} }
    if(e.key==='ArrowRight' || e.key==='d') { if(dir.x===0) dir={x:1,y:0} }
  });

  // touch controls
  const up = document.getElementById('up'); const down = document.getElementById('down');
  const left = document.getElementById('left'); const right = document.getElementById('right');
  up.addEventListener('touchstart',()=>{ if(dir.y===0) dir={x:0,y:-1} });
  down.addEventListener('touchstart',()=>{ if(dir.y===0) dir={x:0,y:1} });
  left.addEventListener('touchstart',()=>{ if(dir.x===0) dir={x:-1,y:0} });
  right.addEventListener('touchstart',()=>{ if(dir.x===0) dir={x:1,y:0} });

  restartBtn.addEventListener('click', restart);

  function restart(){
    snake = [{x:Math.floor(cols/2), y:Math.floor(rows/2)}]; dir={x:1,y:0}; placeApple(); speed=6; running=true; scoreEl.textContent='Score: 0';
  }

  // initial placement
  placeApple(); loop();

})();
