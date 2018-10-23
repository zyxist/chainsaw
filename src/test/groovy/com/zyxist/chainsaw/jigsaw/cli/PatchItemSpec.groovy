package com.zyxist.chainsaw.jigsaw.cli

import spock.lang.Specification

class PatchItemSpec extends Specification {

	def "should not generate a CLI flag, if no JARs are specified"() {
		given:
		def it = new PatchItem('com.example.foo')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.isEmpty()
	}

	def "should correcly handle a case, when a single JAR is specified"() {
		given:
		def it = new PatchItem('com.example.foo').with('/path/to/some.jar')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--patch-module'
		args.get(1) == 'com.example.foo=/path/to/some.jar'
	}

	def "should correctly handle a case, when two or more JARs are specified"() {
		given:
		def it = new PatchItem('com.example.foo')
			.with('/path/to/some.jar')
			.with('/path/to/another.jar')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--patch-module'
		args.get(1) == "com.example.foo=/path/to/some.jar${File.pathSeparator}/path/to/another.jar"
	}

}
