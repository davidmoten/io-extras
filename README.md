# io-extras
<a href="https://github.com/davidmoten/io-extras/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/io-extras/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/io-extras/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/io-extras)<br/>
[![codecov](https://codecov.io/gh/davidmoten/io-extras/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/io-extras)

Java utilities for IO. Requires Java 8+.

Status: *released to Maven Central*

[Release Notes](https://github.com/davidmoten/io-extras/releases)

Maven site reports are [here](http://davidmoten.github.io/io-extras/index.html) including [javadoc](http://davidmoten.github.io/io-extras/apidocs/index.html).

## Features
* OutputStreams as InputStreams using `IOUtil.pipe`
* `BoundedBufferedReader` to trim long lines and avoid OutOfMemoryError calling `readLine()` when line too long
* `Stream<byte[]>` to InputStream using `IOUtil.toInputStream`

## Getting started

Use this maven dependency:

```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>io-extras</artifactId>
  <version>VERSION_HERE</version>
</dependency>
```

## OutputStreams as InputStreams using IOUtil.pipe
If you have a transformation that you can express with `OutputStream`s then you can apply that transformation **synchronously** to an `InputStream` using this library.

An example is you want to pass an `InputStream` to a library but you want that `InputStream` to be compressed with *gzip* as well:

```java
InputStream is = new FileInputStream("myfile.txt");

// zip the input stream!
InputStream gz = IOUtil.pipe(is, o -> new GZIPOutputStream(o));
```

In fact for gzip in particular there is a dedicated method:

```java
InputStream gz = IOUtil.gzip(is);
```

### Options
Internally, the `IOUtil.pipe` method uses a buffer so that data is read into a fixed length byte array. You can specify the `bufferSize` like this:

```java
// set the buffer size (default is 8192)
InputStream gz = IOUtil.pipe(is, o -> new GZIPOutputStream(o), 4096);
```

### Algorithm
Data is passed through the transformation synchronously and this is achieved by reading data from the source `InputStream` and passing that data through a transformed `QueuedOutputStream`. Once bytes are passed through the transformation the output arrays are placed on a queue and the first item on the queue is then used as the input for the next read for the resultant `InputStream`. Given that the amount requested by the client may be less than the size of the first item on the queue, the remnant may be placed back on the first position on the queue ready for the next read. If no data is placed on the queue then more data is read by the source and we continue till something is ready for output.

## InputStreams as OutputStreams
Unfortunately the `InputStream.read` method blocks till a read is available so the only way to handle this is by using another thread. I haven't knocked anything up for this because I think there are products out there already.

## BoundedBufferedReader
When you call `BufferedReader.readLine()` you can run out of memory if the line is too long. In a situation where your code is reading data from an uncontrolled source (like an HTTP POST from a user)
this can crash your web server/program which is obviously undesirable. To trim each line without provoking high memory usage:

```java
BoundedBufferedReader br = new BoundedBufferedReader(reader, bufferSize, maxLineLength);
...
String line = br.readLine();
...
```

Note that `BoundedBufferedReader` does make efficient use of its buffer. The code for the class is a copy and modification of `BufferedReader` from JDK 8.

To test `BoundedBufferedReader` with a really long line call

```bash
mvn clean test -Dn=1000000000
```

`n` is the number of characters in a single long line of input. As a guide n = 2x10^9 takes about 25 seconds to complete. 

## Convert a Stream of byte[] to InputStream

```java
InputStream in = IOUtil.toInputStream(stream);
```

An example of where this is useful is if you wanted to filter lines from an InputStream:

```java
InputStream in = ...;
Stream<byte[]> lines = new BufferedReader(new InputStreamReader(in))
    .lines()
    // filter out empty lines and comment lines
    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
    // not perfect, may add an extra \n to the source!
    // to fix use kool Stream with buffer operator
    .map(line -> (line + "\n").getBytes(StandardCharsets.UTF_8));
InputStream in2 = IOUtil.toInputStream(lines);
``` 
