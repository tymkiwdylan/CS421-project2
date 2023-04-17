package jadx.tests.integration.inline;

import org.assertj.core.api.Condition;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSyntheticBridgeRename extends IntegrationTest {

	@SuppressWarnings("InnerClassMayBeStatic")
	public static class TestCls {
		private abstract class Inner<V> {
			public abstract V get(String value);
		}

		public class IntInner extends Inner<Integer> {
			public Integer get(String value) {
				return value.length();
			}
		}

		public void test() {
			IntInner inner = new IntInner();
			call(inner.get("a"));
		}

		private static void call(Integer value) {
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.JAVA8 })
	public void test() {
		ClassNode cls = getClassNode(TestCls.class);
		assertThat(searchCls(cls.getInnerClasses(), "IntInner").getMethods())
				.as("check that bridge method was generated by compiler")
				.haveAtLeastOne(new Condition<>(mth -> mth.getAccessFlags().isBridge(), "bridge"));

		assertThat(cls)
				.code()
				.doesNotContain("mo0get")
				.containsOne("call(inner.get(\"a\"));");
	}
}
