package com.example.openglcameratest

import android.opengl.GLSurfaceView
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.util.AttributeSet
import android.util.Log
import freemap.openglwrapper.GLMatrix
import freemap.openglwrapper.GPUInterface
import freemap.openglwrapper.OpenGLUtils
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLView(ctx: Context, val textureAvailableCallback: (SurfaceTexture) -> Unit) : GLSurfaceView(ctx), GLSurfaceView.Renderer {


    init {
        setEGLContextClientVersion(2) // specify OpenGL ES 2.0
        setRenderer(this) // set the renderer for this GLSurfaceView
    }

        private lateinit var indices: ShortArray
        lateinit var fbuf: FloatBuffer
    lateinit var indexBuffer: ShortBuffer
    val gpu = GPUInterface("DefaultShaderInterface")
    val viewMatrix = GLMatrix()
    val projectionMatrix = GLMatrix()
        lateinit var cameraFeedSurfaceTexture: SurfaceTexture




    // We initialise the rendering here
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background colour (red=0, green=0, blue=0, alpha=1)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Enable depth testing - will cause nearer 3D objects to automatically
        // be drawn over further objects
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        val textureId = OpenGLUtils.genTexture()

        if (textureId != 0) {
            OpenGLUtils.bindTextureToTextureUnit(textureId, GLES20.GL_TEXTURE0, OpenGLUtils.GL_TEXTURE_EXTERNAL_OES)

            cameraFeedSurfaceTexture = SurfaceTexture(textureId)
            textureAvailableCallback(cameraFeedSurfaceTexture!!)

        }

        try {
            val success = gpu.loadShaders(context.assets, "vertex.glsl", "fragment.glsl")
            if (!success) {
                Log.e("OpenGLBasic", gpu.lastShaderError)
            }
        } catch (e: IOException) {
            Log.e("OpenGLBasic", e.stackTraceToString())
        }

        val vertices = floatArrayOf(
            -1f,1f,0f ,
            -1f,-1f,0f,
            1f,-1f,0f,
            1f,1f,0f,
            1f, 1f, 0f
        )


        fbuf = OpenGLUtils.makeFloatBuffer(vertices)
        indices = shortArrayOf(0,1,2,2,3,0)
        indexBuffer = OpenGLUtils.makeShortBuffer(indices)






    }

    // We draw our shapes here
    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

// Update the surface texture with the latest frame from the camera
        val refTextureUnit = gpu.getUniformLocation("uTexture")
        gpu.setUniformInt(refTextureUnit, 0)
        cameraFeedSurfaceTexture?.updateTexImage()
        gpu.drawIndexedBufferedData(fbuf, indexBuffer, 0, 6)


       // val ref_aVertex = gpu.getAttribLocation("aVertex")
       // val ref_uColour = gpu.getUniformLocation("uColour")
        //val ref_uViewMatrix = gpu.getUniformLocation("uView")
        //gpu.sendMatrix(ref_uViewMatrix, viewMatrix)
        //var ref_uProjMatrix = gpu.getUniformLocation("uProjection")
        //gpu.sendMatrix(ref_uProjMatrix, projectionMatrix)
        //val red = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
        //gpu.setUniform4FloatArray(ref_uColour, red)
        //gpu.specifyBufferedDataFormat(ref_aVertex, fbuf, 0)
        //gpu.drawBufferedTriangles(0, 6)
        //gpu.drawIndexedBufferedData(fbuf, indexBuffer, 0, ref_aVertex)

        //val stride = 24 // because one record contains vertices (12 bytes) and colours (12 bytes)
        //val attrVarRef= gpu.getAttribLocation("aVertex")
        //val colourVarRef = gpu.getAttribLocation("aColour")

       // gpu.specifyBufferedDataFormat(attrVarRef, vertexAndColourBuffer, stride, 0)
        //gpu.specifyBufferedDataFormat(colourVarRef, vertexAndColourBuffer, stride, 3)
        //gpu.drawElements(indices)


        //viewMatrix.setAsIdentityMatrix()


    }

    // Used if the screen is resized
    override fun onSurfaceChanged(unused: GL10, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        val hfov = 60.0f
        val aspect : Float = w.toFloat()/h
        projectionMatrix.setProjectionMatrix(hfov, aspect, 0.001f, 100f)
    }
}