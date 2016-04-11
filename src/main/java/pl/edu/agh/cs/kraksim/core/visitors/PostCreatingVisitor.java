package pl.edu.agh.cs.kraksim.core.visitors;

import pl.edu.agh.cs.kraksim.core.Element;
import pl.edu.agh.cs.kraksim.core.Module;
import pl.edu.agh.cs.kraksim.core.PostCreateOp;
import pl.edu.agh.cs.kraksim.core.exceptions.ExtensionCreationException;

public class PostCreatingVisitor extends UniformElementVisitor {
	private final Module module;

	public PostCreatingVisitor(Module module) {
		this.module = module;
	}

	@Override
	public void visitUniformly(Element element) throws VisitingException {
		try {
			Object ext = element.getExtension(module);
			if (ext instanceof PostCreateOp) {
				((PostCreateOp) ext).postCreate();
			}
		} catch (ExtensionCreationException e) {
			throw new VisitingException(e);
		}
	}
}