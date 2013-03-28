package com.duramec.id.test

import com.duramec.id.{ UUID, EUI48 }
import com.duramec.time.T60Instant
import org.scalatest.FunSuite

class UUIDSuite extends FunSuite {

  test("no Long overflow on Instant conversion") {
    // This is probably the most subtle and destructive bug in this list.
    // text is known to overflow when UUIDGen uses >> instead of >>>
    val text = "2012-07-04T20:14:34.193000000Z"
    val instant = T60Instant.parse(text)
    val uuid = new UUID(EUI48.nil, instant, 0L);
    expectResult (text) {
      uuid.getT60Instant.toString
    }
  }

  test("correct variant") {
    val uuid = new UUID(EUI48.nil, new T60Instant(0L), 0L);
    expectResult (2) { uuid.variant() }
  }

  test("correct version") {
    val uuid = new UUID(EUI48.nil, new T60Instant(0L), 0L);
    expectResult (1) { uuid.version() }
  }

  test("generated from textual representation") {
    val text = "224538e0-c482-11e1-afa2-001c42000009"
    expectResult (text) {
      new UUID(text).toString
    }
  }

  test("proper nil UUID") {
    assert(UUID.nilUUID().toString == "00000000-0000-0000-0000-000000000000")
  }

  test("generated from high and low bytes") {
    val upper = 2469442508703273441L
    val lower = 0L
    val uuid = new UUID(upper, lower)
    val against = new UUID("224538e0-c482-11e1-0000-000000000000")
    expectResult (against) { uuid }
  }

  test("Instants properly stored in UUID and generated back") {
    expectResult (T60Instant.MIN) {
      (new UUID(EUI48.nil, T60Instant.MIN, 0L)).getT60Instant()
    }
    expectResult (T60Instant.MAX) {
      (new UUID(EUI48.nil, T60Instant.MAX, 0L)).getT60Instant()
    }
  }

  test("payload correctly stored") {
    val load1 = 0L;
    expectResult(load1) {
      (new UUID(EUI48.nil, new T60Instant(0L), load1)).getPayload()
    }
    val load2 = 16384L;
    expectResult(0) { // should overflow
      (new UUID(EUI48.nil, new T60Instant(0L), load2)).getPayload()
    }
    val load3 = 16383L;
    expectResult(load3) {
      (new UUID(EUI48.nil, new T60Instant(0L), load3)).getPayload()
    }
    val load4 = 513L;
    expectResult(load4) {
      (new UUID(EUI48.nil, new T60Instant(0L), load4)).getPayload()
    }
  }

  test("generated with node") {
    val node = EUI48.parse("02:26:bb:00:63:49")
    val uuid = new UUID(node, T60Instant.MIN, 0L)
    expectResult (node.asLong) {
      uuid.getNode
    }
    expectResult (EUI48.max.asLong) {
      (new UUID(EUI48.max, T60Instant.MIN, 0L)).getNode()
    }
  }

  test("generated with bytes at time range boundaries") {
    val start = new UUID(EUI48.nil, T60Instant.MIN, 0L)
    expectResult ("00000000-0000-1000-8000-000000000000") {
      start.toString()
    }
    expectResult ("1582-10-15T00:00:00.000000000Z") {
      start.getT60Instant.toString()
    }
    val end = new UUID(EUI48.nil, T60Instant.MAX, 0L)
    expectResult ("ffffffff-ffff-1fff-8000-000000000000") {
      end.toString()
    }
    // Behold, in fear and trembling, the UUPocalypse...
    expectResult ("5236-03-31T21:21:00.684697500Z") {
      end.getT60Instant.toString()
    }
  }

}