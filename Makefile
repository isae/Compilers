RC_RUNTIME = runtime

package: 
	mvn package

test:
	+$(MAKE) -C compiler-tests/core
	+$(MAKE) -C compiler-tests/expressions	
	
clean:
	mvn clean
	+$(MAKE) clean -C compiler-tests/core
	+$(MAKE) clean -C compiler-tests/expressions
	+$(MAKE) -C compiler-tests/deep-expressions
	