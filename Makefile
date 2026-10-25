SRC_DIR      := src
TEST_DIR     := tests
BUILD_DIR    := build
CLASSES_DIR  := $(BUILD_DIR)/classes
TEST_CLASSES := $(BUILD_DIR)/test-classes
MAIN_CLASS   := Main
TEST_CLASS   := TestRunner
JAVAC        ?= javac
JAVA         ?= java

SOURCES := $(shell find $(SRC_DIR) -name '*.java')
TESTS   := $(shell find $(TEST_DIR) -name '*.java')

.PHONY: all build run test clean

all: build

build: $(CLASSES_DIR)/.built

$(CLASSES_DIR)/.built: $(SOURCES)
	@mkdir -p $(CLASSES_DIR)
	$(JAVAC) -d $(CLASSES_DIR) $(SOURCES)
	@touch $@

run: build
	$(JAVA) -cp $(CLASSES_DIR) $(MAIN_CLASS) $(ARGS)

test: build
	@mkdir -p $(TEST_CLASSES)
	$(JAVAC) -cp $(CLASSES_DIR) -d $(TEST_CLASSES) $(TESTS)
	$(JAVA) -cp $(CLASSES_DIR):$(TEST_CLASSES) $(TEST_CLASS)

clean:
	rm -rf $(BUILD_DIR)