package com.phuongthanh.diceroller.core.opengl.shader

import android.opengl.GLES20
import android.util.Log

class DiceShader {

    private var programId: Int = 0

    var aPositionLocation: Int = -1
        private set
    var aNormalLocation: Int = -1
        private set
    var aTexCoordLocation: Int = -1
        private set

    var uMVPMatrixLocation: Int = -1
        private set
    var uMVMatrixLocation: Int = -1
        private set
    var uLightPosLocation: Int = -1
        private set
    var uTextureLocation: Int = -1
        private set
    var uAmbientLocation: Int = -1
        private set
    var uSpecularLocation: Int = -1
        private set

    fun init() {
        val vertexShaderSource = """
            uniform mat4 u_MVPMatrix;
            uniform mat4 u_MVMatrix;

            attribute vec4 a_Position;
            attribute vec3 a_Normal;
            attribute vec2 a_TexCoordinate;

            varying vec3 v_Position;
            varying vec3 v_Normal;
            varying vec2 v_TexCoordinate;

            void main() {
                v_Position = vec3(u_MVMatrix * a_Position);
                v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
                v_TexCoordinate = a_TexCoordinate;
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

        val fragmentShaderSource = """
            precision mediump float;

            uniform sampler2D u_Texture;
            uniform vec3 u_LightPos;
            uniform float u_Ambient;
            uniform float u_Specular;

            varying vec3 v_Position;
            varying vec3 v_Normal;
            varying vec2 v_TexCoordinate;

            void main() {
                vec3 normal = normalize(v_Normal);
                vec3 lightVec = normalize(u_LightPos - v_Position);
                vec3 viewVec = normalize(-v_Position);

                // Ambient lighting
                float ambient = u_Ambient;

                // Diffuse lighting
                float diffuse = max(dot(normal, lightVec), 0.0) * (1.0 - ambient);

                // Specular highlight (tight glint)
                vec3 halfVec = normalize(lightVec + viewVec);
                float specularFactor = pow(max(dot(normal, halfVec), 0.0), 48.0);
                float specular = specularFactor * u_Specular;

                vec4 texColor = texture2D(u_Texture, v_TexCoordinate);
                vec3 litRgb = texColor.rgb * (ambient + diffuse) + vec3(specular);
                gl_FragColor = vec4(litRgb, texColor.a);
            }
        """.trimIndent()

        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)

        programId = GLES20.glCreateProgram()
        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)
        GLES20.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(programId)
            GLES20.glDeleteProgram(programId)
            Log.e("DiceShader", "Error linking program: $error")
            return
        }

        // Cache locations
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        aNormalLocation = GLES20.glGetAttribLocation(programId, "a_Normal")
        aTexCoordLocation = GLES20.glGetAttribLocation(programId, "a_TexCoordinate")

        uMVPMatrixLocation = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        uMVMatrixLocation = GLES20.glGetUniformLocation(programId, "u_MVMatrix")
        uLightPosLocation = GLES20.glGetUniformLocation(programId, "u_LightPos")
        uTextureLocation = GLES20.glGetUniformLocation(programId, "u_Texture")
        uAmbientLocation = GLES20.glGetUniformLocation(programId, "u_Ambient")
        uSpecularLocation = GLES20.glGetUniformLocation(programId, "u_Specular")
    }

    fun use() {
        if (programId != 0) {
            GLES20.glUseProgram(programId)
        }
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val info = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            Log.e("DiceShader", "Shader compile failed: $info")
            return 0
        }
        return shader
    }
}
