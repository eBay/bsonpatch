# This is an implementation of  [RFC 6902 JSON Patch](http://tools.ietf.org/html/rfc6902) written in Java.

This [JSON Patch](http://jsonpatch.com) implementation works directly with [BSON documents](http://bsonspec.org/) using the [MongoDB Java driver implementation of BSON](https://www.mongodb.com/json-and-bson). 

The code here was ported (copied, renamed, repackaged, modified) from the [zjsonpatch project](https://github.com/flipkart-incubator/zjsonpatch).

## Description & Use-Cases
- Java Library to find / apply JSON Patches according to [RFC 6902](http://tools.ietf.org/html/rfc6902).
- JSON Patch defines a JSON document structure for representing changes to a JSON document.
- It can be used to avoid sending a whole document when only a part has changed, thus reducing network bandwidth requirements if data (in JSON format) is required to send across multiple systems over network or in case of multi DC transfer.
- This library compares two [BsonValue](http://mongodb.github.io/mongo-java-driver/3.6/javadoc/org/bson/BsonValue.html) inputs and produces a [BsonArray](http://mongodb.github.io/mongo-java-driver/3.6/javadoc/org/bson/BsonArray.html) of the changes.


### Compatible with : Java 6 and above all versions

## Complexity
- To find JsonPatch : Ω(N+M) ,N and M represents number of keys in first and second JSON respectively / O(summation of la*lb) where la , lb represents JSON Array of length la / lb of against same key in first and second JSON ,since LCS is used to find difference between 2 JSON arrays there of order of quadratic.
- To Optimize Diffs ( compact move and remove into Move ) : Ω(D) / O(D*D) where D represents number of diffs obtained before compaction into Move operation.
- To Apply Diff : O(D) where D represents number of diffs

### How to use:

### Current Version : 0.4.8

Add following to `<dependencies/>` section of your pom.xml -

```xml
<dependency>
  <groupId>com.ebay.bsonpatch</groupId>
  <artifactId>bsonpatch</artifactId>
  <version>0.4.8</version>
</dependency>
```

## API Usage

### Obtaining Json Diff as patch
```xml
BsonArray patch = BsonDiff.asBson(BsonValue source, BsonValue target)
```
Computes and returns a JSON `patch` (as a BsonArray) from `source`  to `target`,
Both `source` and `target` must be either valid BSON objects or arrays or values. 
Further, if resultant `patch` is applied to `source`, it will yield `target`.

The algorithm which computes this JsonPatch currently generates following operations as per [RFC 6902](https://tools.ietf.org/html/rfc6902) -  
 - `add`
 - `remove`
 - `replace`
 - `move`
 - `copy`
 
### Apply Json Patch
```xml
BsonValue target = BsonPatch.apply(BsonArray patch, BsonValue source);
```
Given a `patch`, apply it to `source` Bson and return a `target` Bson which can be ( Bson object or array or value ). This operation  performed on a clone of `source` Bson ( thus, `source` Bson is untouched and can be used further). 

 ## To turn off MOVE & COPY Operations
```xml
EnumSet<DiffFlags> flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone()
BsonArray patch = BsonDiff.asJson(BsonValue source, BsonValue target, flags)
```

### Example
First Json
```json
{"a": 0,"b": [1,2]}
```

Second json ( the json to obtain )
```json
 {"b": [1,2,0]}
```
Following patch will be returned:
```json
[{"op":"move","from":"/a","path":"/b/2"}]
```
here `"op"` represents the operation (`"move"`), `"from"` represent path from where value should be moved, `"path"` represents where value should be moved. The value that is moved is taken as the content at the `"from"` path.

### Apply Json Patch In-Place
```xml
BsonPatch.applyInPlace(BsonArray patch, BsonValue source);
```
Given a `patch`, it will apply it to the `source` BSON mutating the instance, opposed to `BsonPatch.apply` which returns 
a new instance with the patch applied, leaving the `source` unchanged.

### Tests:
1. 100+ selective hardcoded different input JSONs , with their driver test classes present under /test directory.
2. Apart from selective input, a deterministic random JSON generator is present under ( TestDataGenerator.java ),  and its driver test class method is JsonDiffTest.testGeneratedJsonDiff().

#### *** Tests can only show presence of bugs and not their absence ***

## Get Involved

* **Contributing**: Pull requests are welcome!
  * Read [`CONTRIBUTING.md`](CONTRIBUTING.md) 
  * Submit [github issues](https://github.com/eBay/bsonpatch/issues) for any feature enhancements, bugs or documentation problems
    
* **Support**: Questions/comments can posted as [github issues](https://github.com/eBay/bsonpatch/issues)

## Maintainers

* [Dan Douglas](https://github.com/dandoug) 
