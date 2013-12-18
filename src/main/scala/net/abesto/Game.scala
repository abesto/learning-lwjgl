package net.abesto

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.{ContextAttribs, PixelFormat, Display, DisplayMode}
import org.lwjgl.input.Keyboard
import org.lwjgl.BufferUtils

object Game extends App {
  val TARGET_FPS = 60
  val WIDTH = 640
  val HEIGHT = 480
  val VAAI_QUAD_INDICES = 0
  val VAAI_QUAD_COLORS = 1

  lazy val vaoId = glGenVertexArrays()
  lazy val vboVerticesId = glGenBuffers()
  lazy val vboIndicesId = glGenBuffers()
  lazy val vboColorsId = glGenBuffers()

  var indicesCount: Int = -1
  var alpha = 1f
  var alphaDelta = 0.01f
  var alphaDeltaSign = 1
  var quitButtonPressed = false

  setupOpengl()
  setupQuad()
  while (!isCloseRequested) {
    loop()
    Display.sync(TARGET_FPS)
    Display.update()
  }
  destroyQuad()
  destroyOpengl()

  def isCloseRequested = Display.isCloseRequested || quitButtonPressed

  def setupOpengl() {
    Display.setTitle("LWJGL Test")
    Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT))
    Display.create(new PixelFormat(), new ContextAttribs(3, 2)
      .withProfileCore(true)
      .withForwardCompatible(true))
    glViewport(0, 0, WIDTH, HEIGHT)
    glClearColor(0.4f, 0.4f, 0.4f, 1)
    Keyboard.create()
    Keyboard.enableRepeatEvents(true)
  }

  def destroyOpengl() {
    Keyboard.destroy()
    Display.destroy()
  }

  def setupQuad() {
    val vertices = Array(
      -0.5f, 0.5f, 0f, 1f,   // 0: top-left
      -0.5f, -0.5f, 0f, 1f,  // 1: bottom-left
      0.5f, -0.5f, 0f, 1f,   // 2: bottom-right
      0.5f, 0.5f, 0f, 1f     // 3: top-right
    )
    val verticesBuffer = BufferUtils.createFloatBuffer(vertices.length).put(vertices)
    verticesBuffer.flip()

    val indices = Array[Byte](
      0, 1, 2,
      0, 2, 3
    )
    indicesCount = indices.length
    val indicesBuffer = BufferUtils.createByteBuffer(indicesCount).put(indices)
    indicesBuffer.flip()

    glBindVertexArray(vaoId)
    glBindBuffer(GL_ARRAY_BUFFER, vboVerticesId)
    glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
    glVertexAttribPointer(VAAI_QUAD_INDICES, 4, GL_FLOAT, false, 0, 0)
    glBindBuffer(GL_ARRAY_BUFFER, 0)
    glBindVertexArray(0)

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesId)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
  }

  def loop() {
    input()
    logic()
    render()
  }

  def input() {
    while (Keyboard.next()) {
      Keyboard.getEventKey match {
        case Keyboard.KEY_C => quitButtonPressed = true
        case Keyboard.KEY_UP => changeAlphaDelta(0.001f)
        case Keyboard.KEY_DOWN => changeAlphaDelta(-0.001f)
        case default => ()
      }
    }
  }

  def changeAlphaDelta(d: Float) {
    alphaDelta += d
    System.out.println("delta: " + alphaDelta)
  }

  def logic() {
    updateColors(alpha)
    alpha += alphaDelta * alphaDeltaSign
    if (alpha < 0 || alpha > 1) {
      System.out.println(Math.round(alpha))
      alphaDeltaSign *= -1
      alpha += alphaDelta * alphaDeltaSign
    }
  }

  def render() {
    glClear(GL_COLOR_BUFFER_BIT)

    glUseProgram(Shader.programId)

    glBindVertexArray(vaoId)
    glEnableVertexAttribArray(VAAI_QUAD_INDICES)
    glEnableVertexAttribArray(VAAI_QUAD_COLORS)
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesId)

    glDrawElements(GL_TRIANGLES, indicesCount, GL_UNSIGNED_BYTE, 0)

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    glDisableVertexAttribArray(VAAI_QUAD_INDICES)
    glDisableVertexAttribArray(VAAI_QUAD_COLORS)
    glBindVertexArray(0)

    glUseProgram(0)
  }

  def updateColors(alpha: Float) {
    val colors = Array(
      alpha*1f, alpha*0f, alpha*0f, 1f,
      alpha*0f, alpha*1f, alpha*0f, 1f,
      alpha*0f, alpha*0f, alpha*1f, 1f,
      alpha*1f, alpha*1f, alpha*1f, 1f
    )
    val colorsBuffer = BufferUtils.createFloatBuffer(colors.length).put(colors)
    colorsBuffer.flip()

    glBindVertexArray(vaoId)
    glBindBuffer(GL_ARRAY_BUFFER, vboColorsId)
    glBufferData(GL_ARRAY_BUFFER, colorsBuffer, GL_STATIC_DRAW)
    glVertexAttribPointer(VAAI_QUAD_COLORS, 4, GL_FLOAT, false, 0, 0)
    glBindBuffer(GL_ARRAY_BUFFER, 0)
  }

  def destroyQuad() {
    glDeleteBuffers(vboVerticesId)
    glDeleteBuffers(vboIndicesId)
    glDeleteBuffers(vboColorsId)
    glDeleteVertexArrays(vaoId)

    glDetachShader(Shader.programId, Shader.fragmentId)
    glDetachShader(Shader.programId, Shader.vertexId)
    glDeleteShader(Shader.fragmentId)
    glDeleteShader(Shader.vertexId)
    glDeleteProgram(Shader.programId)
  }
}
