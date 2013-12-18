package net.abesto

import org.lwjgl.opengl.GL20

object Shader {
  val FRAGMENT = "fragment.glsl"
  val VERTEX = "vertex.glsl"

  def load(filename: String, shaderType: Int) : Int = {
    val shaderSource = io.Source.fromFile("res/shaders/" + filename).mkString
    val shaderID = GL20.glCreateShader(shaderType)
    GL20.glShaderSource(shaderID, shaderSource)
    GL20.glCompileShader(shaderID)
    shaderID
  }

  val fragmentId = load(FRAGMENT, GL20.GL_FRAGMENT_SHADER)
  val vertexId = load(VERTEX, GL20.GL_VERTEX_SHADER)

  val programId = {
    val programId = GL20.glCreateProgram()
    GL20.glAttachShader(programId, vertexId)
    GL20.glAttachShader(programId, fragmentId)
    GL20.glBindAttribLocation(programId, 0, "in_Position")
    GL20.glBindAttribLocation(programId, 1, "in_Color")
    GL20.glLinkProgram(programId)
    GL20.glValidateProgram(programId)
    programId
  }
}
