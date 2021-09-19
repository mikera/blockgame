package blockgame;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import blockgame.render.Renderer;

public class BlockGame {

	// The window handle
	private long window;
	
	private Renderer renderer=new Renderer();
	
	
    private final boolean[] keysDown = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] keysPressed = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] mousePressed = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];

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
	
	double mouseX;
	double mouseY;

	void init() {
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

		int width=2048;
		int height=1536;
		// Create the window
		window = glfwCreateWindow(width, height, "Convex On-Chain Gaming Demo", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");
		renderer.setSize(width, height);

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			
			keysDown[key]=(action == GLFW_PRESS) || (action == GLFW_REPEAT);
			if (action==GLFW_PRESS) {
				keysPressed[key]=true;
			}
		});
		
		// Grab mouse cursor
		setMouseCapture(true);
		
		
		// Setup mouse cursor callback
		glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
			if (mouseCaptured) {
				double dx=xpos-mouseX;
				double dy=ypos-mouseY;
				renderer.onMouseMove(dx,dy);
				mouseX=xpos;
				mouseY=ypos;
			}
		});
		
		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			if (action==GLFW_PRESS   ) {
				mousePressed[button]=true;
			} else {
				mousePressed[button]=false;
			}
		});
		
		
		// For screen resize etc.
	    glfwSetFramebufferSizeCallback(window, (window,w, h) -> {
	    	renderer.setSize(w,h);
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

	private void loadMousePosition() {
		try(MemoryStack stack=stackPush()){
			DoubleBuffer xp=stack.mallocDouble(1);
			DoubleBuffer yp=stack.mallocDouble(2);
			glfwGetCursorPos(window, xp, yp);
			mouseX=xp.get(0);
			mouseY=yp.get(0);
		}
	}
	
	private boolean mouseCaptured=false;
	private synchronized void setMouseCapture(boolean capture) {
		if (capture) {
			glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
			loadMousePosition();
		} else {
			glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		}
		mouseCaptured=capture;
	}

	public static final long START = System.currentTimeMillis();
	public static long lastTime = 0;

	private void loop() {

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (!glfwWindowShouldClose(window)) {
			long time = (System.currentTimeMillis() - START);
			float t=time*0.001f;
			float dt=(time-lastTime)*0.001f;
			lastTime=time;
			
			updateGame(dt);

			renderer.render(t);

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}
	
	public void updateGame(float dt) {
		float backForward=0.0f;
		
		for (int i=1; i<=9; i++) {
			if (keysDown[GLFW_KEY_0+i]) renderer.applyTool(i);
		}	

		if (keysPressed[GLFW_KEY_E]) {
			setMouseCapture(!mouseCaptured);
			keysPressed[GLFW_KEY_E]=false;
		};

		
		if (keysDown[GLFW_KEY_W]) backForward+=2;
		if (keysDown[GLFW_KEY_S]) backForward-=2;
		
		float leftRight=0.0f;
		if (keysDown[GLFW_KEY_A]) leftRight-=1;
		if (keysDown[GLFW_KEY_D]) leftRight+=1;
		
		float upDown=0.0f;
		if (keysDown[GLFW_KEY_LEFT_SHIFT]) upDown-=1;
		if (keysDown[GLFW_KEY_SPACE]) upDown+=1;

		if (mousePressed[GLFW_MOUSE_BUTTON_RIGHT]==true) {
			renderer.applyRightClick();
			mousePressed[GLFW_MOUSE_BUTTON_RIGHT]=false;
		}
		
		if (mousePressed[GLFW_MOUSE_BUTTON_LEFT]==true) {
			renderer.applyLeftClick();
			mousePressed[GLFW_MOUSE_BUTTON_LEFT]=false;
		}

		
		renderer.applyMove(backForward, leftRight, upDown,dt);
	}


	public static void main(String[] args) {
		new BlockGame().run();
	}

}
