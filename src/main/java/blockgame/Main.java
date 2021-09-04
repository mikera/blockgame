package blockgame;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import org.joml.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import blockgame.render.Renderer;
import blockgame.render.Utils;

public class Main {

	// The window handle
	private long window;
	
	private Renderer renderer=new Renderer();

	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		init();
		loop();

		close();
	}

	private void close() {
		renderer.close();
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Setup mouse cursor callback
		glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
			System.out.println(xpos + "," + ypos);
		});
		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			System.out.println("Mouse button: " + button + ", " + action + ", " + mods);
		});

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		GL.createCapabilities();

		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
		
		renderer.init();


	}

	int createProgram() throws IOException {
		int program = glCreateProgram();
		int vshader = Utils.createShader("shaders/fragment-shader.frag", GL_VERTEX_SHADER);
		int fshader = Utils.createShader("shaders/vertex-shader.vert", GL_FRAGMENT_SHADER);
		glAttachShader(program, vshader);
		glAttachShader(program, fshader);
		glLinkProgram(program);
		int linked = glGetProgrami(program, GL_LINK_STATUS);
		String programLog = glGetProgramInfoLog(program);
		if (programLog.trim().length() > 0) {
			System.err.println(programLog);
		}
		if (linked == 0) {
			throw new AssertionError("Could not link program");
		}
		glUseProgram(program);
		// transformUniform = glGetUniformLocation(program, "transform");
		// glUseProgram(0);
		return program;
	}

	private void loop() {
		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			render();

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

	private static final long START = System.currentTimeMillis();
	FloatBuffer matbuffer = BufferUtils.createFloatBuffer(16);

	private void render() {
		float t = (System.currentTimeMillis() - START) * 0.001f;

		glMatrixMode(GL_MODELVIEW);
		Matrix4f mv = new Matrix4f().rotateY(t);
		mv.get(matbuffer);

		glMatrixMode(GL11.GL_MODELVIEW);
		glLoadMatrixf(matbuffer);

		glDrawArrays(GL_TRIANGLES, 0, 3);
	}

	public static void main(String[] args) {
		new Main().run();
	}

}
