package pl.edu.agh.cs.kraksim.core;

import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.ModuleCreationException;
import pl.edu.agh.cs.kraksim.core.visitors.CreatingVisitor;
import pl.edu.agh.cs.kraksim.core.visitors.PostCreatingVisitor;
import pl.edu.agh.cs.kraksim.core.visitors.VisitingException;

/* 
 * Module
 * 
 * Module groups set of extensions working together to offer
 * some functionality.
 * 
 * In our architecture this grouping is only logical. In general
 * extensions of the same module do not reference each other.
 * They only reference to and are referenced by core elements,
 * which they extend.
 * 
 * To get an extension for a given element, some kind of a handle
 * is needed to specify which module to get an extension from.
 * A module object represents only this *handle*.
 */
@SuppressWarnings("unchecked")
public class Module {

	/* name, for informative purposes */
	private final String name;

	private final Core core;

	/*
	   * Extensions to every element are stored in an array. See Element.extensions
	   * Extensions of this module are stored in key-th cell of this array.
	   */
	final int key;

	/* Set of extension classes */
	final EntityClassSet extClassSet;

	/* See Core.newModule() */
	Module(String name, Core core, int key, Class<? extends ModuleCreator> creatorClass) throws InvalidClassSetDefException {
		this(name, core, key, EntityClassSet.createFromCreatorClass(creatorClass));
	}

	/* See Core.newModule() */
	Module(String name, Core core, int key, final ModuleCreator creator) throws InvalidClassSetDefException, ModuleCreationException {
		this(name, core, key, EntityClassSet.createFromCreatorClass(creator.getClass()));

		creator.setModule(this);
		try {
			City city = core.getCity();
			city.applyElementVisitor(new CreatingVisitor(this, creator));
			city.applyElementVisitor(new PostCreatingVisitor(this));
		} catch (VisitingException e) {
			throw new ModuleCreationException("cannot create module " + name, e);
		}
	}

	private Module(String name, Core core, int key, EntityClassSet extClassSet) throws InvalidClassSetDefException {
		this.name = name;
		this.core = core;
		this.extClassSet = extClassSet;
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public Core getCore() {
		return core;
	}

	public EntityClassSet getExtensionClasses() {
		return extClassSet;
	}

	//CONCRETE_ELEMENT_TYPE_DEPENDENT


}
