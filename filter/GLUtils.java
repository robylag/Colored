package com.example.coloredapp.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class GLUtils {

    private static final float[] vertexData = {
            -1f,  1f,   0f, 0f,
            -1f, -1f,   0f, 1f,
            1f,  1f,   1f, 0f,
            1f, -1f,   1f, 1f
    };

    // Programas e locations para shader OES (textura externa)
    private static int oesProgram;
    private static int oesPositionLocation;
    private static int oesTexCoordLocation;
    private static int oesTextureMatrixLocation;
    private static int oesTextureSamplerLocation;
    private static int oesFilterTypeLocation;
    private static int oesMVPMatrixLocation;

    private static final String oesVertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "uniform mat4 uSTMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec2 a_TexCoord;" +
                    "varying vec2 v_TexCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * a_Position;" +
                    "  v_TexCoord = (uSTMatrix * vec4(a_TexCoord, 0.0, 1.0)).xy;" +
                    "}";




    private static final String oesFragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES u_TextureSampler;\n" +
                    "uniform int u_FilterType;\n" +
                    "varying vec2 v_TexCoord;\n" +
                    "void main() {\n" +
                    "  vec4 color = texture2D(u_TextureSampler, v_TexCoord);\n" +
                    "  if (u_FilterType == 1) {\n" +
                    "    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));\n" +
                    "    color = vec4(gray, gray, gray, 1.0);\n" +
                    "  } else if (u_FilterType == 2) {\n" +
                    "    color = vec4(0.567, 0.433, color.b, 1.0);\n" +  // protanopia simulation
                    "  } else if (u_FilterType == 3) {\n" +
                    "    color = vec4(color.r, 0.500, 0.500, 1.0);\n" +  // deuteranopia simulation
                    "  } else if (u_FilterType == 4) {\n" +
                    "    color = vec4(color.r, color.g, 0.25, 1.0);\n" +  // tritanopia simulation
                    "  }\n" +
                    "  gl_FragColor = color;\n" +
                    "}";

    // IDs do framebuffer e textura para FBO
    private static final int[] fbo = new int[1];
    private static final int[] fboTexture = new int[1];

    public static void init() {
        int oesVertexShader = compileShader(GLES20.GL_VERTEX_SHADER, oesVertexShaderCode);
        int oesFragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, oesFragmentShaderCode);

        oesProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(oesProgram, oesVertexShader);
        GLES20.glAttachShader(oesProgram, oesFragmentShader);
        GLES20.glLinkProgram(oesProgram);

        // Aqui você obtém os locations dos atributos e uniforms
        oesPositionLocation = GLES20.glGetAttribLocation(oesProgram, "a_Position");
        oesTexCoordLocation = GLES20.glGetAttribLocation(oesProgram, "a_TexCoord");
        oesMVPMatrixLocation = GLES20.glGetUniformLocation(oesProgram, "uMVPMatrix");       // <---- aqui
        oesTextureMatrixLocation = GLES20.glGetUniformLocation(oesProgram, "uSTMatrix");     // <---- e aqui
        oesTextureSamplerLocation = GLES20.glGetUniformLocation(oesProgram, "u_TextureSampler");
        oesFilterTypeLocation = GLES20.glGetUniformLocation(oesProgram, "u_FilterType");
    }


    public static int createOESTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return textureId;
    }

    public static void drawTextureWithFilter(int textureId, int filterType, float[] textureMatrix) {
        GLES20.glUseProgram(oesProgram);

        // Passa a matriz identidade para uMVPMatrix (para o posicionamento dos vértices)
        GLES20.glUniformMatrix4fv(oesMVPMatrixLocation, 1, false, identityMatrix(), 0);

        // Passa a matriz de transformação da textura (uSTMatrix)
        GLES20.glUniformMatrix4fv(oesTextureMatrixLocation, 1, false, textureMatrix, 0);

        // Configura o atributo de posição dos vértices (x,y)
        GLES20.glVertexAttribPointer(oesPositionLocation, 2, GLES20.GL_FLOAT, false, 16, createFloatBuffer(vertexData));
        GLES20.glEnableVertexAttribArray(oesPositionLocation);

        // Configura o atributo de coordenadas de textura (u,v)
        GLES20.glVertexAttribPointer(oesTexCoordLocation, 2, GLES20.GL_FLOAT, false, 16, createFloatBuffer(vertexData, 2));
        GLES20.glEnableVertexAttribArray(oesTexCoordLocation);

        // Passa o filtro para o shader
        GLES20.glUniform1i(oesFilterTypeLocation, filterType);

        // Ativa a textura e vincula a textura externa OES
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(oesTextureSamplerLocation, 0);

        // Desenha o quadrilátero como TRIANGLE_STRIP
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }


    // ... restante do código permanece igual ...

    private static java.nio.FloatBuffer createFloatBuffer(float[] data) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocateDirect(data.length * 4);
        bb.order(java.nio.ByteOrder.nativeOrder());
        java.nio.FloatBuffer fb = bb.asFloatBuffer();
        fb.put(data);
        fb.position(0);
        return fb;
    }

    private static java.nio.FloatBuffer createFloatBuffer(float[] data, int offset) {
        int count = data.length / 4;
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocateDirect(count * 2 * 4);
        bb.order(java.nio.ByteOrder.nativeOrder());
        java.nio.FloatBuffer fb = bb.asFloatBuffer();
        for (int i = 0; i < count; i++) {
            fb.put(data[i * 4 + offset]);
            fb.put(data[i * 4 + offset + 1]);
        }
        fb.position(0);
        return fb;
    }

    private static int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile failed: " + log);
        }
        return shader;
    }

    private static float[] identityMatrix() {
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        Matrix.scaleM(m, 0, 1f, -1f, 1f);
        Matrix.translateM(m, 0, 0f, -1f, 0f);
        return m;
    }

    public static int getFBOId() {
        return fbo[0];
    }

    public static int getFBOTextureId() {
        return fboTexture[0];
    }
    public static void resizeFBO(int w, int h) {
        // implementação
    }

}
