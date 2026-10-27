package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

@Name("Script Name")
@Description({
	"The name of a script, including its path from the scripts folder but without the '.sk' extension.",
	"For example, the script 'games/murder mystery.sk' is named 'games/murder mystery'."
})
@Example("""
	on script load:
		broadcast "Loaded %script's name%"
	""")
@Example("set {_name} to the name of the script named \"weather.sk\"")
@Since("2.0")
public class ExprScriptName extends SimplePropertyExpression<Script, String> {

	static {
		register(ExprScriptName.class, String.class, "name", "scripts");
	}

	@Override
	public @Nullable String convert(Script script) {
		return script.nameAndPath();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "name";
	}

}
