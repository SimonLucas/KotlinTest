package breakoutJS

// import games.breakout.BreakoutGameState
// import agents.SimpleEvoAgent
import jquery.jq
import mymath.Vec2d
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Math

/*
This example is based on example from html5 canvas2D docs:
  http://www.w3.org/TR/2dcontext/
Note that only a subset of the api is supported for now.

see example here:  https://try.kotlinlang.org/#/Examples/Canvas/Creatures/Creatures.kt


*/

fun main(args: Array<String>) {
    jq {
        HelloWorld().run()
        // FancyLines().run()
    }
}

// todo : Run the AI to play the game
// todo : Measure the speed of the forward model running in JS
// todo : Capture keyboard events

val canvas = initalizeCanvas()

fun initalizeCanvas(): HTMLCanvasElement {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    context.canvas.width  = window.innerWidth // 800
    context.canvas.height = window.innerHeight / 2 // 400

    document.body!!.appendChild(canvas)
    return canvas
}


class HelloWorld() {
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    val height = canvas.height
    val width = canvas.width

    val gameState: BreakoutGameState = BreakoutGameState().setUp()


    val square = width / 5.0

    var hue = 0.0
    var hueInc = 1
    // var mouseString = "mouse"
    var mousePos: Vec2d = Vec2d(width/2.0, 0.0)

    // val agent = SimpleEvoAgent()

    var keyStr: String = "key"

    init {

        jq(canvas).mousemove {
            mousePos = Vec2d(it.pageX - canvas.offsetLeft, it.pageY - canvas.offsetTop)

            keyStr = "ll"
            // mouse = "[${it.pageX - canvas.offsetLeft}, ${it.pageY- canvas.offsetTop}]"
        }

        document.onkeypress = {e ->
            // var test = e.toString() //  + keyStr
            // keyStr = test // "doc: ${ it.type }"
            // return
            // keyStr = "hello"
        }

        window.onkeypress = {
            // keyStr = "cc"
            // keyStr = "ll"
        }

//        // window.addEventListener("keydown", e -> "e )
//
//        window.addEventListener("keydown", function (e) {
//            myGameArea.key = e.keyCode;
//        })
//        window.addEventListener("keyup", function (e) {
//            myGameArea.key = false;
//        })
//

        // cannot attach it to a window it seems
//        jq(window).mousemove {
//            mousePos = Vec2d(it.pageX - canvas.offsetLeft, it.pageY - canvas.offsetTop)
//
//            // mouse = "[${it.pageX - canvas.offsetLeft}, ${it.pageY- canvas.offsetTop}]"
//        }

    }

    fun testRect() {
        var x = (width-square) * Math.random();
        var y = (height-square) * Math.random();

        gameState.state.bat.x = mousePos.x / width
        hue += hueInc
        if (hue > 255) hue = 0.0
        // context.fillRect(0.0, 0.0, width.toDouble(), height.toDouble());
        // context.fillRect(x, y, square, square);

//        window.onmousemove = {
//
//        }

        // val action = agent.getAction(gameState, 0)

        blank()
        drawWall()
        drawBat()
        drawBall()
        drawScore()

        gameState.next(intArrayOf(Constants.doNothing), 0)
        if (gameState.isTerminal()) gameState.reset()
    }

    fun drawBall() {
        val s = gameState.state.ball.s
        val rad = gameState.state.params.ballSize

        val cx = s.x * width
        val cy = s.y * height

        val pixWidth = width * rad
        val pixHeight = height * rad

        context.fillStyle = "white" // "hsl($hue, 50%, 50%)";
        context.fillRect (cx-pixWidth/2, cy-pixHeight/2, pixWidth, pixHeight);
    }

    fun drawBat() {
        // would be better to extract the common code
        val params = gameState.state.params
        val s = gameState.state.bat
        // System.out.println("Bat: " + s);
        val cx = s.x * width
        val cy = s.y * height

        val pixWidth = width * (params.batWidth - params.ballSize)
        val pixHeight = height * (params.batHeight - params.ballSize)
        context.fillStyle = "hsl(128, 100%, 50%)"
        context.fillRect(cx-pixWidth/2, cy-pixHeight/2, pixWidth, pixHeight)
    }

    fun drawScore() {
        context.font = "30px Comic Sans MS";
        context.fillStyle = "red";
        context.textAlign = CanvasTextAlign.CENTER
        // mouseString = "${mousePos.x}, $"

//        window.onmousemove {
//
//        }
//
//        val str = "Score = ${gameState.score().toInt()}, mouse = ${mousePos.x}, ${mousePos.y}"
        val str = "Score = ${gameState.score().toInt()}"
        context.fillText(str, width/2.0, height/10.0);
    }

    fun drawWall() {
        with (gameState.state) {
            val cellWidth = width / params.gridWidth
            val cellHeight = height / params.gridHeight
            for (i in 0 until params.gridWidth) {
                for (j in 0 until params.gridHeight) {
                    val cx = (i + 0.5) * cellWidth
                    val cy = (j + 0.5) * cellHeight

                    val pixWidth = cellWidth - width * params.ballSize
                    val pixHeight = cellHeight - height * params.ballSize

//                    g.setColor(getBrickColor(i, j))
                    context.fillStyle = "rgba(0, 0, 0, 0.1)"

                    if (bricks[i][j] != Constants.empytyCell) {
                        val brickHue = (j * 500) / params.gridHeight
                        // context.fillStyle = "rgba(0, 255, 0, 0.5)" // "hsl($brickHue, 50%, 50%)";
                        context.fillStyle = "hsl($brickHue, 100%, 60%)";
                    }
                    context.fillRect(cx-pixWidth/2, cy-pixHeight/2, pixWidth, pixHeight)
                }
            }
        }
    }

    fun blank() {
        // context.fillStyle = "rgba(255,255,128,0.1)";
        context.fillStyle = "rgba(0, 0, 0, 0.5)";
        context.fillRect(0.0, 0.0, width.toDouble(), height.toDouble());
    }

    fun run() {
        window.setInterval({ testRect() }, 20);
        // window.setInterval({ blank() }, 100);
    }

}



//
//class HelloWorld() {
//    val context = canvas.getContext("2d") as CanvasRenderingContext2D
//    val height = canvas.height
//    val width = canvas.width
//    // var x = width * Random.nextDouble()
//    // var y = height * Random.nextDouble()
//    var hue = 0;
//
//}

class FancyLines() {
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    val height = canvas.height
    val width = canvas.width
    var x = width * Math.random()
    var y = height * Math.random()
    var hue = 0;

    fun line() {
        context.save();

        context.beginPath();

        context.lineWidth = 20.0 * Math.random();
        context.moveTo(x, y);

        x = width * Math.random();
        y = height * Math.random();

        context.bezierCurveTo(width * Math.random(), height * Math.random(),
                width * Math.random(), height * Math.random(), x, y);

        hue += (Math.random() * 10).toInt();

        context.strokeStyle = "hsl($hue, 50%, 50%)";

        context.shadowColor = "white";
        context.shadowBlur = 10.0;

        context.stroke();

        context.restore();
    }

    fun drawWall() {


    }

    fun blank() {
        context.fillStyle = "rgba(255,255,1,0.1)";
        context.fillRect(0.0, 0.0, width.toDouble(), height.toDouble());
    }

    fun message() {
        context.fillStyle = "rgba(255,0,1,0.5)";
        val mess = "Message"; // mytest.Message().randomMessage()
        context.strokeText(mess, 20.0, 30.0)
    }

    fun run() {
        window.setInterval({ line() }, 40);
        window.setInterval({ blank() }, 100);
        window.setInterval({ message() }, 200);
    }
}
