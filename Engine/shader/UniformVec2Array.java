package shader;

import org.joml.Vector2f;

public class UniformVec2Array extends Uniform{
	
	private UniformVec2[] uniforms;
	
	public UniformVec2Array(String name, int size) {
		super(name);
		uniforms = new UniformVec2[size];
		for(int i=0;i<size;i++){
			uniforms[i] = new UniformVec2(name + "["+i+"]");
		}
	}
	
	@Override
	protected void storeUniformLocation(int programID) {
		for(UniformVec2 matrixUniform : uniforms){
			matrixUniform.storeUniformLocation(programID);
		}
	}

	public void loadVec2(Vector2f[] vecs){
		for(int i=0;i<vecs.length;i++){
			uniforms[i].loadVec2(vecs[i]);
		}
	}
	
	public void loadVec2(int index, float x, float y) {
		uniforms[index].loadVec2(x,y);
	}
}
