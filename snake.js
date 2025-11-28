// 贪吃蛇游戏 JavaScript 逻辑
const canvas = document.getElementById('game-canvas');
const ctx = canvas.getContext('2d');
const scoreDisplay = document.getElementById('score');

// 游戏配置
const gridSize = 20;
const gridWidth = canvas.width / gridSize;
const gridHeight = canvas.height / gridSize;

// 初始状态
let snake = [
    {x: Math.floor(gridWidth / 2), y: Math.floor(gridHeight / 2)}
];
let food = generateFood();
let direction = 'right';
let nextDirection = 'right';
let score = 0;
let gameSpeed = 150; // 毫秒
let gameRunning = true;

function generateFood() {
    let newFood;
    do {
        newFood = {
            x: Math.floor(Math.random() * gridWidth),
            y: Math.floor(Math.random() * gridHeight)
        };
    } while (snake.some(segment => segment.x === newFood.x && segment.y === newFood.y));
    return newFood;
}

function draw() {
    // 清空画布
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // 绘制蛇
    snake.forEach((segment, index) => {
        ctx.fillStyle = index === 0 ? '#2e8b57' : '#3cb371';
        ctx.fillRect(segment.x * gridSize, segment.y * gridSize, gridSize, gridSize);
        ctx.strokeStyle = '#1e5b3a';
        ctx.strokeRect(segment.x * gridSize, segment.y * gridSize, gridSize, gridSize);
    });
    
    // 绘制食物
    ctx.fillStyle = '#ff6347';
    ctx.fillRect(food.x * gridSize, food.y * gridSize, gridSize, gridSize);
}

function update() {
    if (!gameRunning) return;
    
    direction = nextDirection;
    
    // 计算新头部位置
    const head = {...snake[0]};
    switch (direction) {
        case 'up': head.y--; break;
        case 'down': head.y++; break;
        case 'left': head.x--; break;
        case 'right': head.x++; break;
    }
    
    // 检查碰撞边界
    if (head.x < 0 || head.x >= gridWidth || head.y < 0 || head.y >= gridHeight) {
        gameOver();
        return;
    }
    
    // 检查碰撞自身
    if (snake.some(segment => segment.x === head.x && segment.y === head.y)) {
        gameOver();
        return;
    }
    
    // 添加新头部
    snake.unshift(head);
    
    // 检查是否吃到食物
    if (head.x === food.x && head.y === food.y) {
        score += 10;
        scoreDisplay.textContent = `得分: ${score}`;
        food = generateFood();
        // 提高难度
        if (gameSpeed > 50) {
            gameSpeed -= 2;
        }
    } else {
        // 移除尾部
        snake.pop();
    }
}

function gameOver() {
    gameRunning = false;
    alert(`游戏结束！最终得分: ${score}`);
    // 重置游戏
    setTimeout(() => {
        snake = [{x: Math.floor(gridWidth / 2), y: Math.floor(gridHeight / 2)}];
        food = generateFood();
        direction = 'right';
        nextDirection = 'right';
        score = 0;
        scoreDisplay.textContent = `得分: ${score}`;
        gameSpeed = 150;
        gameRunning = true;
        gameLoop();
    }, 100);
}

function gameLoop() {
    if (!gameRunning) return;
    update();
    draw();
    setTimeout(gameLoop, gameSpeed);
}

// 键盘控制
document.addEventListener('keydown', (e) => {
    switch (e.key) {
        case 'ArrowUp':
            if (direction !== 'down') nextDirection = 'up';
            break;
        case 'ArrowDown':
            if (direction !== 'up') nextDirection = 'down';
            break;
        case 'ArrowLeft':
            if (direction !== 'right') nextDirection = 'left';
            break;
        case 'ArrowRight':
            if (direction !== 'left') nextDirection = 'right';
            break;
    }
});

// 启动游戏
gameLoop();