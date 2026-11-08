SRC_DIR = src
TEST_DIR = tests
OUT_DIR = out
MAIN_CLASS = Main
TEST_CLASS = TestRunner

.PHONY: build run test clean

build:
	@mkdir -p $(OUT_DIR)
	javac -d $(OUT_DIR) $(SRC_DIR)/*.java $(SRC_DIR)/config/*.java $(TEST_DIR)/*.java

run: build
	java -cp $(OUT_DIR) $(MAIN_CLASS) $(ARGS)

test: build
	java -cp $(OUT_DIR) $(TEST_CLASS)

clean:
	@rm -rf $(OUT_DIR)