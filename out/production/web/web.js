if (typeof kotlin === 'undefined') {
  throw new Error("Error loading module 'web'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'web'.");
}
var web = function (_, Kotlin) {
  'use strict';
  var Kind_OBJECT = Kotlin.Kind.OBJECT;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var L0 = Kotlin.Long.ZERO;
  var Unit = Kotlin.kotlin.Unit;
  var numberToInt = Kotlin.numberToInt;
  var to = Kotlin.kotlin.to_ujzrz7$;
  var hashMapOf = Kotlin.kotlin.collections.hashMapOf_qfcya0$;
  var throwCCE = Kotlin.throwCCE;
  var ensureNotNull = Kotlin.ensureNotNull;
  var Kind_INTERFACE = Kotlin.Kind.INTERFACE;
  var toString = Kotlin.toString;
  function Constants() {
    Constants_instance = this;
    this.doNothing = 0;
    this.left = 1;
    this.right = 2;
    this.empytyCell = 0;
  }
  Constants.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Constants',
    interfaces: []
  };
  var Constants_instance = null;
  function Constants_getInstance() {
    if (Constants_instance === null) {
      new Constants();
    }
    return Constants_instance;
  }
  function BreakoutParams(gridWidth, gridHeight, batWidth, batHeight, topGap, wallBottom, edgeGap, maxTicks, ballSpeed, ballSize, batSpeed, batInfluence) {
    if (gridWidth === void 0)
      gridWidth = 11;
    if (gridHeight === void 0)
      gridHeight = 15;
    if (batWidth === void 0)
      batWidth = 0.1;
    if (batHeight === void 0)
      batHeight = 0.05;
    if (topGap === void 0)
      topGap = 0.2;
    if (wallBottom === void 0)
      wallBottom = 0.5;
    if (edgeGap === void 0)
      edgeGap = 0.0;
    if (maxTicks === void 0)
      maxTicks = 50000;
    if (ballSpeed === void 0)
      ballSpeed = 0.01;
    if (ballSize === void 0)
      ballSize = batHeight / 4;
    if (batSpeed === void 0)
      batSpeed = 0.01;
    if (batInfluence === void 0)
      batInfluence = 2.0;
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.batWidth = batWidth;
    this.batHeight = batHeight;
    this.topGap = topGap;
    this.wallBottom = wallBottom;
    this.edgeGap = edgeGap;
    this.maxTicks = maxTicks;
    this.ballSpeed = ballSpeed;
    this.ballSize = ballSize;
    this.batSpeed = batSpeed;
    this.batInfluence = batInfluence;
  }
  BreakoutParams.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'BreakoutParams',
    interfaces: []
  };
  BreakoutParams.prototype.component1 = function () {
    return this.gridWidth;
  };
  BreakoutParams.prototype.component2 = function () {
    return this.gridHeight;
  };
  BreakoutParams.prototype.component3 = function () {
    return this.batWidth;
  };
  BreakoutParams.prototype.component4 = function () {
    return this.batHeight;
  };
  BreakoutParams.prototype.component5 = function () {
    return this.topGap;
  };
  BreakoutParams.prototype.component6 = function () {
    return this.wallBottom;
  };
  BreakoutParams.prototype.component7 = function () {
    return this.edgeGap;
  };
  BreakoutParams.prototype.component8 = function () {
    return this.maxTicks;
  };
  BreakoutParams.prototype.component9 = function () {
    return this.ballSpeed;
  };
  BreakoutParams.prototype.component10 = function () {
    return this.ballSize;
  };
  BreakoutParams.prototype.component11 = function () {
    return this.batSpeed;
  };
  BreakoutParams.prototype.component12 = function () {
    return this.batInfluence;
  };
  BreakoutParams.prototype.copy_ofy3u8$ = function (gridWidth, gridHeight, batWidth, batHeight, topGap, wallBottom, edgeGap, maxTicks, ballSpeed, ballSize, batSpeed, batInfluence) {
    return new BreakoutParams(gridWidth === void 0 ? this.gridWidth : gridWidth, gridHeight === void 0 ? this.gridHeight : gridHeight, batWidth === void 0 ? this.batWidth : batWidth, batHeight === void 0 ? this.batHeight : batHeight, topGap === void 0 ? this.topGap : topGap, wallBottom === void 0 ? this.wallBottom : wallBottom, edgeGap === void 0 ? this.edgeGap : edgeGap, maxTicks === void 0 ? this.maxTicks : maxTicks, ballSpeed === void 0 ? this.ballSpeed : ballSpeed, ballSize === void 0 ? this.ballSize : ballSize, batSpeed === void 0 ? this.batSpeed : batSpeed, batInfluence === void 0 ? this.batInfluence : batInfluence);
  };
  BreakoutParams.prototype.toString = function () {
    return 'BreakoutParams(gridWidth=' + Kotlin.toString(this.gridWidth) + (', gridHeight=' + Kotlin.toString(this.gridHeight)) + (', batWidth=' + Kotlin.toString(this.batWidth)) + (', batHeight=' + Kotlin.toString(this.batHeight)) + (', topGap=' + Kotlin.toString(this.topGap)) + (', wallBottom=' + Kotlin.toString(this.wallBottom)) + (', edgeGap=' + Kotlin.toString(this.edgeGap)) + (', maxTicks=' + Kotlin.toString(this.maxTicks)) + (', ballSpeed=' + Kotlin.toString(this.ballSpeed)) + (', ballSize=' + Kotlin.toString(this.ballSize)) + (', batSpeed=' + Kotlin.toString(this.batSpeed)) + (', batInfluence=' + Kotlin.toString(this.batInfluence)) + ')';
  };
  BreakoutParams.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.gridWidth) | 0;
    result = result * 31 + Kotlin.hashCode(this.gridHeight) | 0;
    result = result * 31 + Kotlin.hashCode(this.batWidth) | 0;
    result = result * 31 + Kotlin.hashCode(this.batHeight) | 0;
    result = result * 31 + Kotlin.hashCode(this.topGap) | 0;
    result = result * 31 + Kotlin.hashCode(this.wallBottom) | 0;
    result = result * 31 + Kotlin.hashCode(this.edgeGap) | 0;
    result = result * 31 + Kotlin.hashCode(this.maxTicks) | 0;
    result = result * 31 + Kotlin.hashCode(this.ballSpeed) | 0;
    result = result * 31 + Kotlin.hashCode(this.ballSize) | 0;
    result = result * 31 + Kotlin.hashCode(this.batSpeed) | 0;
    result = result * 31 + Kotlin.hashCode(this.batInfluence) | 0;
    return result;
  };
  BreakoutParams.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.gridWidth, other.gridWidth) && Kotlin.equals(this.gridHeight, other.gridHeight) && Kotlin.equals(this.batWidth, other.batWidth) && Kotlin.equals(this.batHeight, other.batHeight) && Kotlin.equals(this.topGap, other.topGap) && Kotlin.equals(this.wallBottom, other.wallBottom) && Kotlin.equals(this.edgeGap, other.edgeGap) && Kotlin.equals(this.maxTicks, other.maxTicks) && Kotlin.equals(this.ballSpeed, other.ballSpeed) && Kotlin.equals(this.ballSize, other.ballSize) && Kotlin.equals(this.batSpeed, other.batSpeed) && Kotlin.equals(this.batInfluence, other.batInfluence)))));
  };
  var Array_0 = Array;
  function InternalGameState(params, ball, bat, score, nBricks, nTicks, gameOver) {
    if (params === void 0)
      params = new BreakoutParams();
    if (ball === void 0)
      ball = new MovableObject();
    if (bat === void 0)
      bat = Vector2d_init();
    if (score === void 0)
      score = 0;
    if (nBricks === void 0)
      nBricks = 0;
    if (nTicks === void 0)
      nTicks = 0;
    if (gameOver === void 0)
      gameOver = false;
    this.params = params;
    this.ball = ball;
    this.bat = bat;
    this.score = score;
    this.nBricks = nBricks;
    this.nTicks = nTicks;
    this.gameOver = gameOver;
    var array = Array_0(this.params.gridWidth);
    var tmp$;
    tmp$ = array.length - 1 | 0;
    for (var i = 0; i <= tmp$; i++) {
      array[i] = new Int32Array(this.params.gridHeight);
    }
    this.bricks = array;
  }
  InternalGameState.prototype.initBall = function () {
    var s = Vector2d_init(0.5, this.params.wallBottom + this.params.batWidth);
    var v = Vector2d_init(this.params.ballSpeed / 2, this.params.ballSpeed);
    this.ball = new MovableObject(s, v);
    return this.ball;
  };
  InternalGameState.prototype.initBat = function () {
    var s = Vector2d_init(0.5, 1.0 - 2 * this.params.batHeight);
    this.bat = s;
    return this.bat;
  };
  InternalGameState.prototype.deepCopy = function () {
    var tmp$;
    var state = this.copy_m7rekw$();
    state.bricks = this.bricks.slice();
    state.ball = this.ball.copy();
    state.bat = this.bat.copy();
    tmp$ = this.bricks.length;
    for (var i = 0; i < tmp$; i++) {
      state.bricks[i] = this.bricks[i].slice();
    }
    return state;
  };
  InternalGameState.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'InternalGameState',
    interfaces: []
  };
  InternalGameState.prototype.component1 = function () {
    return this.params;
  };
  InternalGameState.prototype.component2 = function () {
    return this.ball;
  };
  InternalGameState.prototype.component3 = function () {
    return this.bat;
  };
  InternalGameState.prototype.component4 = function () {
    return this.score;
  };
  InternalGameState.prototype.component5 = function () {
    return this.nBricks;
  };
  InternalGameState.prototype.component6 = function () {
    return this.nTicks;
  };
  InternalGameState.prototype.component7 = function () {
    return this.gameOver;
  };
  InternalGameState.prototype.copy_m7rekw$ = function (params, ball, bat, score, nBricks, nTicks, gameOver) {
    return new InternalGameState(params === void 0 ? this.params : params, ball === void 0 ? this.ball : ball, bat === void 0 ? this.bat : bat, score === void 0 ? this.score : score, nBricks === void 0 ? this.nBricks : nBricks, nTicks === void 0 ? this.nTicks : nTicks, gameOver === void 0 ? this.gameOver : gameOver);
  };
  InternalGameState.prototype.toString = function () {
    return 'InternalGameState(params=' + Kotlin.toString(this.params) + (', ball=' + Kotlin.toString(this.ball)) + (', bat=' + Kotlin.toString(this.bat)) + (', score=' + Kotlin.toString(this.score)) + (', nBricks=' + Kotlin.toString(this.nBricks)) + (', nTicks=' + Kotlin.toString(this.nTicks)) + (', gameOver=' + Kotlin.toString(this.gameOver)) + ')';
  };
  InternalGameState.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.params) | 0;
    result = result * 31 + Kotlin.hashCode(this.ball) | 0;
    result = result * 31 + Kotlin.hashCode(this.bat) | 0;
    result = result * 31 + Kotlin.hashCode(this.score) | 0;
    result = result * 31 + Kotlin.hashCode(this.nBricks) | 0;
    result = result * 31 + Kotlin.hashCode(this.nTicks) | 0;
    result = result * 31 + Kotlin.hashCode(this.gameOver) | 0;
    return result;
  };
  InternalGameState.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.params, other.params) && Kotlin.equals(this.ball, other.ball) && Kotlin.equals(this.bat, other.bat) && Kotlin.equals(this.score, other.score) && Kotlin.equals(this.nBricks, other.nBricks) && Kotlin.equals(this.nTicks, other.nTicks) && Kotlin.equals(this.gameOver, other.gameOver)))));
  };
  var totalTicks;
  function BreakoutGameState(state) {
    if (state === void 0)
      state = new InternalGameState();
    this.state = state;
    this.brickValues = hashMapOf([to(0, 100), to(1, 100), to(2, 100), to(3, 50), to(4, 30), to(5, 20)]);
    this.defaultScore = 10;
  }
  BreakoutGameState.prototype.resetTotalTicks = function () {
    totalTicks = L0;
  };
  BreakoutGameState.prototype.totalTicks = function () {
    return totalTicks;
  };
  BreakoutGameState.prototype.copy = function () {
    return new BreakoutGameState(this.state.deepCopy());
  };
  BreakoutGameState.prototype.brickValue_za3lpa$ = function (j) {
    var score = this.brickValues.get_11rb$(j);
    if (score != null)
      return score;
    else
      return this.defaultScore;
  };
  BreakoutGameState.prototype.reset = function () {
    this.state = new InternalGameState();
    this.setUp();
    return this;
  };
  BreakoutGameState.prototype.setUp = function () {
    var $receiver = this.state;
    $receiver.ball = $receiver.initBall();
    $receiver.bat = $receiver.initBat();
    var $receiver_0 = $receiver.params;
    var tmp$, tmp$_0;
    tmp$ = $receiver_0.gridWidth;
    for (var i = 0; i < tmp$; i++) {
      tmp$_0 = $receiver_0.gridHeight;
      for (var j = 0; j < tmp$_0; j++) {
        var x = (i + 0.5) / $receiver_0.gridWidth;
        var y = (j + 0.5) / $receiver_0.gridHeight;
        var isBrick = x >= $receiver_0.edgeGap && x <= 1 - $receiver_0.edgeGap && y >= $receiver_0.topGap && y <= $receiver_0.wallBottom;
        $receiver.bricks[i][j] = isBrick ? 1 : 0;
      }
    }
    return this;
  };
  var Math_0 = Math;
  BreakoutGameState.prototype.next_u4kcgn$ = function (actions, playerId) {
    var action = actions[0];
    var $receiver = this.state;
    var tmp$;
    var receiver = $receiver.params;
    var flipX = {v: false};
    var flipY = {v: false};
    var receiver_0 = this.state.ball;
    var nextS = Vector2d_init_0(receiver_0.s);
    nextS.add_kkby1b$(receiver_0.v);
    if (nextS.y >= 1.0) {
      $receiver.gameOver = true;
      $receiver.score = $receiver.score - 50 | 0;
    }
    if (nextS.x <= 0 || nextS.x >= 1)
      flipX.v = true;
    if (nextS.y <= 0 || nextS.y >= 1)
      flipY.v = true;
    var inArena = !flipX.v && !flipY.v;
    if (inArena) {
      var bx = this.gridX_14dthe$(nextS.x);
      var by = this.gridY_14dthe$(nextS.y);
      if ($receiver.bricks[bx][by] !== Constants_getInstance().empytyCell) {
        $receiver.bricks[bx][by] = Constants_getInstance().empytyCell;
        $receiver.score = $receiver.score + this.brickValue_za3lpa$(by) | 0;
        if (this.gridX_14dthe$(receiver_0.s.x) !== this.gridX_14dthe$(nextS.x))
          flipX.v = true;
        if (this.gridY_14dthe$(receiver_0.s.y) !== this.gridY_14dthe$(nextS.y))
          flipY.v = true;
      }
    }
    if (flipX.v)
      receiver_0.v.x = -receiver_0.v.x;
    if (flipY.v)
      receiver_0.v.y = -receiver_0.v.y;
    var xDiff = nextS.x - $receiver.bat.x;
    if (Math_0.abs(xDiff) < receiver.batWidth / 2 && nextS.y > $receiver.bat.y - receiver.batHeight / 2 && nextS.y < $receiver.bat.y + receiver.batHeight / 2) {
      receiver_0.v.y = -receiver.ballSpeed;
      receiver_0.v.x = xDiff * receiver.batInfluence * receiver.ballSpeed / receiver.batWidth;
    }
    receiver_0.s.add_kkby1b$(receiver_0.v);
    var receiver_1 = this.state.bat;
    if (action === Constants_getInstance().left)
      receiver_1.x -= receiver.batSpeed;
    if (action === Constants_getInstance().right)
      receiver_1.x += receiver.batSpeed;
    if (receiver_1.x >= 1.0)
      receiver_1.x = 1.0;
    if (receiver_1.x < 0.0)
      receiver_1.x = 0.0;
    tmp$ = $receiver.nTicks, $receiver.nTicks = tmp$ + 1 | 0;
    totalTicks = totalTicks.inc();
    return this;
  };
  BreakoutGameState.prototype.gridX_14dthe$ = function (x) {
    return numberToInt(x * this.state.params.gridWidth);
  };
  BreakoutGameState.prototype.gridY_14dthe$ = function (y) {
    return numberToInt(y * this.state.params.gridHeight);
  };
  BreakoutGameState.prototype.nActions = function () {
    return 3;
  };
  BreakoutGameState.prototype.score = function () {
    return this.state.score;
  };
  BreakoutGameState.prototype.isTerminal = function () {
    return this.state.gameOver || this.state.nTicks >= this.state.params.maxTicks;
  };
  BreakoutGameState.prototype.nTicks = function () {
    return this.state.nTicks;
  };
  BreakoutGameState.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'BreakoutGameState',
    interfaces: [ExtendedAbstractGameState]
  };
  BreakoutGameState.prototype.component1 = function () {
    return this.state;
  };
  BreakoutGameState.prototype.copy_qyx8k4$ = function (state) {
    return new BreakoutGameState(state === void 0 ? this.state : state);
  };
  BreakoutGameState.prototype.toString = function () {
    return 'BreakoutGameState(state=' + Kotlin.toString(this.state) + ')';
  };
  BreakoutGameState.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.state) | 0;
    return result;
  };
  BreakoutGameState.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && Kotlin.equals(this.state, other.state))));
  };
  function main$lambda() {
    (new HelloWorld()).run();
    return Unit;
  }
  function main(args) {
    $(main$lambda);
  }
  var canvas;
  function initalizeCanvas() {
    var tmp$, tmp$_0;
    var canvas = Kotlin.isType(tmp$ = document.createElement('canvas'), HTMLCanvasElement) ? tmp$ : throwCCE();
    var context = Kotlin.isType(tmp$_0 = canvas.getContext('2d'), CanvasRenderingContext2D) ? tmp$_0 : throwCCE();
    context.canvas.width = 600;
    context.canvas.height = 300;
    ensureNotNull(document.body).appendChild(canvas);
    return canvas;
  }
  function HelloWorld() {
    var tmp$;
    this.context = Kotlin.isType(tmp$ = canvas.getContext('2d'), CanvasRenderingContext2D) ? tmp$ : throwCCE();
    this.height = canvas.height;
    this.width = canvas.width;
    this.gameState = (new BreakoutGameState()).setUp();
    this.square = this.width / 5.0;
    this.hue = 0.0;
    this.hueInc = 1;
  }
  HelloWorld.prototype.testRect = function () {
    var x = (this.width - this.square) * Math.random();
    var y = (this.height - this.square) * Math.random();
    this.context.fillStyle = 'hsl(' + this.hue + ', 50%, 50%)';
    this.hue += this.hueInc;
    if (this.hue > 255)
      this.hue = 0.0;
    var ball = this.gameState.state.ball.s;
    this.context.fillRect(ball.x * this.width, ball.y * this.height, this.square, this.square);
    this.gameState.next_u4kcgn$(new Int32Array([Constants_getInstance().doNothing]), 0);
    if (this.gameState.isTerminal())
      this.gameState.reset();
  };
  HelloWorld.prototype.blank = function () {
    this.context.fillStyle = 'rgba(255,255,1,0.1)';
    this.context.fillRect(0.0, 0.0, this.width, this.height);
  };
  function HelloWorld$run$lambda(this$HelloWorld) {
    return function () {
      this$HelloWorld.testRect();
      return Unit;
    };
  }
  function HelloWorld$run$lambda_0(this$HelloWorld) {
    return function () {
      this$HelloWorld.blank();
      return Unit;
    };
  }
  HelloWorld.prototype.run = function () {
    window.setInterval(HelloWorld$run$lambda(this), 20);
    window.setInterval(HelloWorld$run$lambda_0(this), 100);
  };
  HelloWorld.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'HelloWorld',
    interfaces: []
  };
  function FancyLines() {
    var tmp$;
    this.context = Kotlin.isType(tmp$ = canvas.getContext('2d'), CanvasRenderingContext2D) ? tmp$ : throwCCE();
    this.height = canvas.height;
    this.width = canvas.width;
    this.x = this.width * Math.random();
    this.y = this.height * Math.random();
    this.hue = 0;
  }
  FancyLines.prototype.line = function () {
    this.context.save();
    this.context.beginPath();
    this.context.lineWidth = 20.0 * Math.random();
    this.context.moveTo(this.x, this.y);
    this.x = this.width * Math.random();
    this.y = this.height * Math.random();
    this.context.bezierCurveTo(this.width * Math.random(), this.height * Math.random(), this.width * Math.random(), this.height * Math.random(), this.x, this.y);
    this.hue = this.hue + numberToInt(Math.random() * 10) | 0;
    this.context.strokeStyle = 'hsl(' + this.hue + ', 50%, 50%)';
    this.context.shadowColor = 'white';
    this.context.shadowBlur = 10.0;
    this.context.stroke();
    this.context.restore();
  };
  FancyLines.prototype.blank = function () {
    this.context.fillStyle = 'rgba(255,255,1,0.1)';
    this.context.fillRect(0.0, 0.0, this.width, this.height);
  };
  FancyLines.prototype.message = function () {
    this.context.fillStyle = 'rgba(255,0,1,0.5)';
    var mess = 'Message';
    this.context.strokeText(mess, 20.0, 30.0);
  };
  function FancyLines$run$lambda(this$FancyLines) {
    return function () {
      this$FancyLines.line();
      return Unit;
    };
  }
  function FancyLines$run$lambda_0(this$FancyLines) {
    return function () {
      this$FancyLines.blank();
      return Unit;
    };
  }
  function FancyLines$run$lambda_1(this$FancyLines) {
    return function () {
      this$FancyLines.message();
      return Unit;
    };
  }
  FancyLines.prototype.run = function () {
    window.setInterval(FancyLines$run$lambda(this), 40);
    window.setInterval(FancyLines$run$lambda_0(this), 100);
    window.setInterval(FancyLines$run$lambda_1(this), 200);
  };
  FancyLines.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'FancyLines',
    interfaces: []
  };
  function SimplePlayerInterface() {
  }
  SimplePlayerInterface.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'SimplePlayerInterface',
    interfaces: []
  };
  function AbstractGameState() {
  }
  AbstractGameState.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'AbstractGameState',
    interfaces: []
  };
  function ExtendedAbstractGameState() {
  }
  ExtendedAbstractGameState.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'ExtendedAbstractGameState',
    interfaces: [AbstractGameState]
  };
  function MovableObject(s, v) {
    if (s === void 0)
      s = Vector2d_init();
    if (v === void 0)
      v = Vector2d_init();
    this.s = s;
    this.v = v;
  }
  MovableObject.prototype.update_c0y6xt$ = function (resultantForce, lossFactor) {
    this.v.add_kkby1b$(resultantForce);
    this.s.add_kkby1b$(this.v);
    this.v.mul_14dthe$(lossFactor);
    return this;
  };
  MovableObject.prototype.toString = function () {
    return this.s.toString() + ' : ' + this.v;
  };
  MovableObject.prototype.copy = function () {
    return new MovableObject(this.s.copy(), this.v.copy());
  };
  MovableObject.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'MovableObject',
    interfaces: []
  };
  MovableObject.prototype.component1 = function () {
    return this.s;
  };
  MovableObject.prototype.component2 = function () {
    return this.v;
  };
  MovableObject.prototype.copy_cfxg1e$ = function (s, v) {
    return new MovableObject(s === void 0 ? this.s : s, v === void 0 ? this.v : v);
  };
  MovableObject.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.s) | 0;
    result = result * 31 + Kotlin.hashCode(this.v) | 0;
    return result;
  };
  MovableObject.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.s, other.s) && Kotlin.equals(this.v, other.v)))));
  };
  function v(x, y) {
    return new Vec2d(x, y);
  }
  function Vec2d(x, y) {
    if (x === void 0)
      x = 0.0;
    if (y === void 0)
      y = 0.0;
    this.x = x;
    this.y = y;
  }
  Vec2d.prototype.plus_j6393o$ = function (v_0) {
    return v(this.x + v_0.x, this.y + v_0.y);
  };
  Vec2d.prototype.unaryMinus = function () {
    return v(-this.x, -this.y);
  };
  Vec2d.prototype.minus_j6393o$ = function (v_0) {
    return v(this.x - v_0.x, this.y - v_0.y);
  };
  Vec2d.prototype.times_14dthe$ = function (koef) {
    return v(this.x * koef, this.y * koef);
  };
  Vec2d.prototype.distanceTo_j6393o$ = function (v) {
    var x = this.minus_j6393o$(v).sqr;
    return Math_0.sqrt(x);
  };
  Vec2d.prototype.rotatedBy_14dthe$ = function (theta) {
    var sin = Math_0.sin(theta);
    var cos = Math_0.cos(theta);
    return v(this.x * cos - this.y * sin, this.x * sin + this.y * cos);
  };
  Vec2d.prototype.isInRect_miaiek$ = function (topLeft, size) {
    return this.x >= topLeft.x && this.x <= topLeft.x + size.x && this.y >= topLeft.y && this.y <= topLeft.y + size.y;
  };
  Object.defineProperty(Vec2d.prototype, 'sqr', {
    get: function () {
      return this.x * this.x + this.y * this.y;
    }
  });
  Object.defineProperty(Vec2d.prototype, 'normalized', {
    get: function () {
      var x = this.sqr;
      return this.times_14dthe$(1.0 / Math_0.sqrt(x));
    }
  });
  Vec2d.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Vec2d',
    interfaces: []
  };
  function Vector2d() {
    this.x = 0;
    this.y = 0;
  }
  Vector2d.prototype.equals = function (o) {
    var tmp$;
    if (Kotlin.isType(o, Vector2d)) {
      var v = (tmp$ = o) == null || Kotlin.isType(tmp$, Vector2d) ? tmp$ : throwCCE();
      return this.x === ensureNotNull(v).x && this.y === v.y;
    }
     else {
      return false;
    }
  };
  Vector2d.prototype.copy = function () {
    return Vector2d_init(this.x, this.y);
  };
  Vector2d.prototype.set_kkby1b$ = function (v) {
    this.x = v.x;
    this.y = v.y;
    return this;
  };
  Vector2d.prototype.set_lu1900$ = function (x, y) {
    this.x = x;
    this.y = y;
    return this;
  };
  Vector2d.prototype.zero = function () {
    this.x = 0.0;
    this.y = 0.0;
    return this;
  };
  Vector2d.prototype.toString = function () {
    return this.x.toString() + ' : ' + toString(this.y);
  };
  Vector2d.prototype.add_kkby1b$ = function (v) {
    this.x = this.x + v.x;
    this.y = this.y + v.y;
    return this;
  };
  Vector2d.prototype.add_lu1900$ = function (x, y) {
    this.x = this.x + x;
    this.y = this.y + y;
    return this;
  };
  Vector2d.prototype.add_c0y6xt$ = function (v, w) {
    this.x = this.x + w * v.x;
    this.y = this.y + w * v.y;
    return this;
  };
  Vector2d.prototype.wrap_lu1900$ = function (w, h) {
    this.x = (this.x + w) % w;
    this.y = (this.y + h) % h;
    return this;
  };
  Vector2d.prototype.subtract_kkby1b$ = function (v) {
    this.x = this.x - v.x;
    this.y = this.y - v.y;
    return this;
  };
  Vector2d.prototype.subtract_lu1900$ = function (x, y) {
    this.x = this.x - x;
    this.y = this.y - y;
    return this;
  };
  Vector2d.prototype.mul_14dthe$ = function (fac) {
    this.x *= fac;
    this.y *= fac;
    return this;
  };
  Vector2d.prototype.rotate_14dthe$ = function (theta) {
    var cosTheta = Math_0.cos(theta);
    var sinTheta = Math_0.sin(theta);
    var nx = this.x * cosTheta - this.y * sinTheta;
    var ny = this.x * sinTheta + this.y * cosTheta;
    this.x = nx;
    this.y = ny;
    return this;
  };
  Vector2d.prototype.contraRotate_cfxg1e$ = function (start, end) {
    var r = start.dist_kkby1b$(end);
    var cosTheta = (end.x - start.x) / r;
    var sinTheta = (end.y - start.y) / r;
    var nx = this.x * cosTheta + this.y * sinTheta;
    var ny = -this.x * sinTheta + this.y * cosTheta;
    this.x = nx;
    this.y = ny;
    return this;
  };
  Vector2d.prototype.contraRotate_kkby1b$ = function (heading) {
    var r = heading.mag();
    var cosTheta = heading.y / r;
    var sinTheta = heading.x / r;
    var nx = this.x * cosTheta + this.y * sinTheta;
    var ny = -this.x * sinTheta + this.y * cosTheta;
    this.x = nx;
    this.y = ny;
    return this;
  };
  Vector2d.prototype.sqr_14dthe$ = function (x) {
    return x * x;
  };
  Vector2d.prototype.scalarProduct_kkby1b$ = function (v) {
    return this.x * v.x + this.y * v.y;
  };
  Vector2d.prototype.sqDist_kkby1b$ = function (v) {
    return this.sqr_14dthe$(this.x - v.x) + this.sqr_14dthe$(this.y - v.y);
  };
  Vector2d.prototype.mag = function () {
    var x = this.sqr_14dthe$(this.x) + this.sqr_14dthe$(this.y);
    return Math_0.sqrt(x);
  };
  Vector2d.prototype.dist_kkby1b$ = function (v) {
    var x = this.sqDist_kkby1b$(v);
    return Math_0.sqrt(x);
  };
  Vector2d.prototype.theta = function () {
    var y = this.y;
    var x = this.x;
    return Math_0.atan2(y, x);
  };
  Vector2d.prototype.normalise = function () {
    var mag = this.mag();
    if (mag !== 0.0) {
      this.x /= mag;
      this.y /= mag;
    }
  };
  Vector2d.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Vector2d',
    interfaces: []
  };
  function Vector2d_init(x, y, $this) {
    if (x === void 0)
      x = 0.0;
    if (y === void 0)
      y = 0.0;
    $this = $this || Object.create(Vector2d.prototype);
    Vector2d.call($this);
    $this.x = x;
    $this.y = y;
    return $this;
  }
  function Vector2d_init_0(v, $this) {
    $this = $this || Object.create(Vector2d.prototype);
    Vector2d.call($this);
    $this.x = v.x;
    $this.y = v.y;
    return $this;
  }
  var package$breakoutJS = _.breakoutJS || (_.breakoutJS = {});
  Object.defineProperty(package$breakoutJS, 'Constants', {
    get: Constants_getInstance
  });
  package$breakoutJS.BreakoutParams = BreakoutParams;
  package$breakoutJS.InternalGameState = InternalGameState;
  Object.defineProperty(package$breakoutJS, 'totalTicks', {
    get: function () {
      return totalTicks;
    },
    set: function (value) {
      totalTicks = value;
    }
  });
  package$breakoutJS.BreakoutGameState = BreakoutGameState;
  package$breakoutJS.main_kand9s$ = main;
  Object.defineProperty(package$breakoutJS, 'canvas', {
    get: function () {
      return canvas;
    }
  });
  package$breakoutJS.initalizeCanvas = initalizeCanvas;
  package$breakoutJS.HelloWorld = HelloWorld;
  package$breakoutJS.FancyLines = FancyLines;
  var package$ggiJS = _.ggiJS || (_.ggiJS = {});
  package$ggiJS.SimplePlayerInterface = SimplePlayerInterface;
  package$ggiJS.AbstractGameState = AbstractGameState;
  package$ggiJS.ExtendedAbstractGameState = ExtendedAbstractGameState;
  var package$mymath = _.mymath || (_.mymath = {});
  package$mymath.MovableObject = MovableObject;
  package$mymath.v_lu1900$ = v;
  package$mymath.Vec2d = Vec2d;
  package$mymath.Vector2d_init_lu1900$ = Vector2d_init;
  package$mymath.Vector2d_init_kkby1b$ = Vector2d_init_0;
  package$mymath.Vector2d = Vector2d;
  totalTicks = L0;
  canvas = initalizeCanvas();
  main([]);
  Kotlin.defineModule('web', _);
  return _;
}(typeof web === 'undefined' ? {} : web, kotlin);
