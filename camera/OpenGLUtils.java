package com.example.coloredapp.camera;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

// Classe utilitária para facilitar operações com OpenGL ES 2.0 e aplicar filtros
public class OpenGLUtils {

    private static final String TAG = "OpenGLUtils";

    // Vertex Shader — define como os vértices serão posicionados e como aplicar a matriz de transformação à textura
    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;" +
                    "attribute vec2 aTexCoord;" +
                    "uniform mat4 uTextureMatrix;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "    gl_Position = aPosition;" +
                    "    vTexCoord = (uTextureMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;" +
                    "}";

    // Fragment Shader — Sem filtro (mostra a imagem original da câmera)
    private static final String FRAGMENT_SHADER_NO_FILTER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES uTexture;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "    gl_FragColor = texture2D(uTexture, vTexCoord);" +
                    "}";

    // Fragment Shader — Simula Protanopia (cegueira ao vermelho)
    private static final String FRAGMENT_SHADER_PROTANOPIA =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES uTexture;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "    vec4 color = texture2D(uTexture, vTexCoord);" +
                    "    mat3 protanopia = mat3(" +
                    "        0.567, 0.433, 0.000," +
                    "        0.558, 0.442, 0.000," +
                    "        0.000, 0.242, 0.758);" +
                    "    vec3 filtered = protanopia * color.rgb;" +
                    "    gl_FragColor = vec4(filtered, color.a);" +
                    "}";

    // Fragment Shader — Simula Deuteranopia (cegueira ao verde)
    private static final String FRAGMENT_SHADER_DEUTERANOPIA =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES uTexture;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "    vec4 color = texture2D(uTexture, vTexCoord);" +
                    "    mat3 deuteranopia = mat3(" +
                    "        0.625, 0.375, 0.000," +
                    "        0.700, 0.300, 0.000," +
                    "        0.000, 0.300, 0.700);" +
                    "    vec3 filtered = deuteranopia * color.rgb;" +
                    "    gl_FragColor = vec4(filtered, color.a);" +
                    "}";

    // Fragment Shader — Simula Tritanopia (cegueira ao azul)
    private static final String FRAGMENT_SHADER_TRITANOPIA =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES uTexture;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "    vec4 color = texture2D(uTexture, vTexCoord);" +
                    "    mat3 tritanopia = mat3(" +
                    "        0.950, 0.050, 0.000," +
                    "        0.000, 0.433, 0.567," +
                    "        0.000, 0.475, 0.525);" +
                    "    vec3 filtered = tritanopia * color.rgb;" +
                    "    gl_FragColor = vec4(filtered, color.a);" +
                    "}";

    // Cria e configura uma textura externa (usada com SurfaceTexture da câmera)
    public static int createOESTexture() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    // Cria um programa de shader baseado no filtro selecionado
    public static int createCameraShaderProgram(CameraRenderer.FilterType filterType) {
        String fragmentShaderSource;

        // Seleciona o fragment shader correspondente ao filtro
        switch (filterType) {
            case PROTANOPIA:
                fragmentShaderSource = FRAGMENT_SHADER_PROTANOPIA;
                break;
            case DEUTERANOPIA:
                fragmentShaderSource = FRAGMENT_SHADER_DEUTERANOPIA;
                break;
            case TRITANOPIA:
                fragmentShaderSource = FRAGMENT_SHADER_TRITANOPIA;
                break;
            case NONE:
            default:
                fragmentShaderSource = FRAGMENT_SHADER_NO_FILTER;
                break;
        }

        return compileShaders(VERTEX_SHADER, fragmentShaderSource);
    }

    // Compila vertex e fragment shaders e cria um programa
    public static int compileShaders(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            String error = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            Log.e(TAG, "Erro ao linkar o programa: " + error);
            throw new RuntimeException("Erro ao linkar o programa: " + error);
        }

        return program;
    }

    // Compila um shader individual (vertex ou fragment)
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            Log.e(TAG, "Erro ao compilar shader (" +
                    (type == GLES20.GL_VERTEX_SHADER ? "VERTEX" : "FRAGMENT") + "): " + error);
            throw new RuntimeException("Erro ao compilar shader: " + error);
        }

        return shader;
    }

    // Cria um FloatBuffer a partir de um array de floats
    public static FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4); // cada float = 4 bytes
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(coords).position(0);
        return buffer;
    }

    // Cria buffer para os vértices (cobre toda a tela)
    public static FloatBuffer createVertexBuffer() {
        float[] vertexCoords = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f,  1.0f,
                1.0f,  1.0f
        };
        return createFloatBuffer(vertexCoords);
    }

    // Cria buffer para as coordenadas da textura
    public static FloatBuffer createTexCoordBuffer() {
        float[] texCoords = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        return createFloatBuffer(texCoords);
    }

    // Renderiza um frame usando os shaders, textura da câmera e buffers de posição/textura
    public static void drawFrame(int program, int oesTextureId, float[] transformMatrix,
                                 FloatBuffer vertexBuffer, FloatBuffer texCoordBuffer) {

        GLES20.glUseProgram(program); // Usa o programa de shader

        // Recupera os locais das variáveis no shader
        int aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        int aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord");
        int uTextureMatrix = GLES20.glGetUniformLocation(program, "uTextureMatrix");
        int uTexture = GLES20.glGetUniformLocation(program, "uTexture");

        // Passa os dados de posição dos vértices
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // Passa os dados de coordenadas da textura
        GLES20.glEnableVertexAttribArray(aTexCoord);
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        // Envia a matriz de transformação da textura
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0);

        // Ativa a textura da câmera
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId);
        GLES20.glUniform1i(uTexture, 0);

        // Desenha o frame com 4 vértices (strip)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // Limpa os atributos
        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aTexCoord);
    }
}