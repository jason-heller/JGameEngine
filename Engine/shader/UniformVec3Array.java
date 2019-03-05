package shader;

import org.joml.Vector3f;

public class UniformVec3Array extends Uniform{
	
	private UniformVec3[] uniforms;
	
	public UniformVec3Array(String name, int size) {
		super(name);
		uniforms = new UniformVec3[size];
		for(int i=0;i<size;i++){
			uniforms[i] = new UniformVec3(name + "["+i+"]");
		}
	}
	
	@Override
	protected void storeUniformLocation(int programID) {
		for(UniformVec3 matrixUniform : uniforms){
			matrixUniform.storeUniformLocation(programID);
		}
	}

	public void loadVec3(Vector3f[] vecs){
		for(int i=0;i<vecs.length;i++){
			uniforms[i].loadVec3(vecs[i]);
		}
	}
	
	public void loadVec3(int index, float x, float y, float z) {
		uniforms[index].loadVec3(x,y,z);
	}
}
