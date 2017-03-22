TESTS=test001 test002 test003 test004 test005 test006 test007 test008 test009 test010 test011 test012 test013 test014 test015 test016 test017 test018 test019 test020 test021 test022 test023 test024 test025 test026

core-tests = compiler-tests/core
runtime = runtime

.PHONY: check $(TESTS) 

package: 
	mvn package
	rm -rf $(runtime)
	mkdir $(runtime)
	check $(TESTS) 
	
clean:
	mvn clean
	rm -rf $(runtime)
	
check: $(TESTS) 

$(TESTS): %: %.expr
	cat $(core-tests)/$@.input | rc -i $< > $(runtime)/$@.log && diff $(runtime)/$@.log $(core-tests)/orig/$@.log
	cat $(core-tests)/$@.input | rc -s $< > $(runtime)/$@.log && diff $(runtime)/$@.log $(core-tests)/orig/$@.log	