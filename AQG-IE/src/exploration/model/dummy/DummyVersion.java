package exploration.model.dummy;

import exploration.model.Version;
import exploration.model.enumerations.VersionEnum;

public class DummyVersion extends Version {

	public DummyVersion(String name) {

		super(0, VersionEnum.valueOf(name), null);

	}

}
