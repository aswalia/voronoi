class Point {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }
}

const canvas = document.getElementById('voronoiCanvas');
const ctx = canvas.getContext('2d');
const points = [];

// Handle resizing like ZoomPanController.java
function resize() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight - 50;
    computeVoronoi();
}
window.addEventListener('resize', resize);
resize();

function addRandomPoints() {
    for(let i=0; i<10; i++) {
        points.push(new Point(
            Math.random() * canvas.width,
            Math.random() * canvas.height
        ));
    }
    updateStatus();
    draw();
}

function clearCanvas() {
    points.length = 0;
    updateStatus();
    draw();
}

function updateStatus() {
    document.getElementById('status').innerText = `Points: ${points.length}`;
}

// Drawing logic adapted from AnimationView.java
function draw(edges = []) {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  renderEdges(edges);

  ctx.fillStyle = '#ff4444';
  points.forEach(p => {
    ctx.beginPath();
    ctx.arc(p.x, p.y, 3, 0, Math.PI * 2);
    ctx.fill();
  });
}

async function computeVoronoi() {
  if (points.length < 3) {
    lastEdges = [];
    draw([]);
    return;
  }

  const payload = { width: canvas.width, height: canvas.height, points };
  const response = await fetch('/compute', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    console.error('compute failed', response.status, await response.text());
    return;
  }

  lastEdges = await response.json();   // array of edges
  console.log('edges', lastEdges.length);
  draw(lastEdges);
}

function renderEdges(edges) {
    ctx.strokeStyle = '#2196F3';
    ctx.lineWidth = 2;
    ctx.setLineDash([]); // Solid lines for the final diagram
    
    edges.forEach(edge => {
        ctx.beginPath();
        ctx.moveTo(edge.x1, edge.y1);
        ctx.lineTo(edge.x2, edge.y2);
        ctx.stroke();
    });
}

// Trigger computation on every click (like your interactive JavaFX app)
canvas.addEventListener('mousedown', (e) => {
    const rect = canvas.getBoundingClientRect();
    points.push(new Point(e.clientX - rect.left, e.clientY - rect.top));
    updateStatus();
    draw();
    computeVoronoi(); 
});

let animationFrames = [];
let animationIndex = 0;
let animationTimer = null;

async function fetchAnimation() {
  if (points.length < 3) return;

  const payload = { width: canvas.width, height: canvas.height, points };
  const resp = await fetch('/animate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });

  if (!resp.ok) {
    console.error(await resp.text());
    return;
  }

  const data = await resp.json();
  animationFrames = data.frames || [];
  animationIndex = 0;

  console.log("frames:", animationFrames.length);
}

function playAnimation(fps = 10) {
  stopAnimation();
  if (!animationFrames.length) return;

  const interval = Math.max(1, Math.floor(1000 / fps));
  animationTimer = setInterval(() => {
    const frame = animationFrames[animationIndex];
    if (!frame) return;

    draw(frame.edges);              // draw edges + points
    // optional: show label
    //document.getElementById('status').innerText = `Frame ${frame.index}: ${frame.label}`;

    animationIndex++;
    if (animationIndex >= animationFrames.length) stopAnimation();
  }, interval);
}

function stopAnimation() {
  if (animationTimer) {
    clearInterval(animationTimer);
    animationTimer = null;
  }
}
