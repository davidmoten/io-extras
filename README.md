# io-extras
<a href="https://travis-ci.org/davidmoten/io-extras"><img src="https://travis-ci.org/davidmoten/io-extras.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/io-extras/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/io-extras)<br/>
[![codecov](https://codecov.io/gh/davidmoten/io-extras/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/io-extras)

Java utilities for IO. Requires Java 8+.

Status: *released to Maven Central*

[Release Notes](https://github.com/davidmoten/io-extras/releases)

Maven site reports are [here](http://davidmoten.github.io/io-extras/index.html) including [javadoc](http://davidmoten.github.io/io-extras/apidocs/index.html).

## Getting started

Use this maven dependency:

```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>io-extras</artifactId>
  <version>0.1</version>
</dependency>
```

## OutputStreams as InputStreams using IOUtil.pipe
If you have a transformation that you can express with `OutputStream`s then you can apply that transformation *synchronously* to an `InputStream` using this library.

An example is you want to pass an `InputStream` to a library but you want that `InputStream` to be compressed with *gzip* as well:

```java
InputStream is = new FileInputStream("myfile.txt");

// zip the input stream!
InputStream gz = IOUtil.pipe(is, o -> new GZIPOutputStream(o));

// upload the zipped input stream to an AWS S3 object
s3.putObject(bucket, "myfile.txt.gz", gz, metadata);

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
