if (typeof kotlin === 'undefined') {
  throw new Error("Error loading module 'web'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'web'.");
}
var web = function (_, Kotlin) {
  'use strict';
  var Unit = Kotlin.kotlin.Unit;
  var throwCCE = Kotlin.throwCCE;
  var ensureNotNull = Kotlin.ensureNotNull;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var numberToInt = Kotlin.numberToInt;
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
    this.context.fillRect(x, y, this.square, this.square);
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
  var package$breakoutJS = _.breakoutJS || (_.breakoutJS = {});
  package$breakoutJS.main_kand9s$ = main;
  Object.defineProperty(package$breakoutJS, 'canvas', {
    get: function () {
      return canvas;
    }
  });
  package$breakoutJS.initalizeCanvas = initalizeCanvas;
  package$breakoutJS.HelloWorld = HelloWorld;
  package$breakoutJS.FancyLines = FancyLines;
  canvas = initalizeCanvas();
  main([]);
  Kotlin.defineModule('web', _);
  return _;
}(typeof web === 'undefined' ? {} : web, kotlin);
