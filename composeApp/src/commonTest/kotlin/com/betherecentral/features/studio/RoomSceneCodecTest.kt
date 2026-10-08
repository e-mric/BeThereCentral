package com.betherecentral.features.studio

import com.betherecentral.features.studio.domain.FurnitureKind
import com.betherecentral.features.studio.domain.RoomScene
import com.betherecentral.features.studio.domain.RoomSceneCodec
import com.betherecentral.features.studio.domain.StudioObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RoomSceneCodecTest {
    private val sample = RoomScene(
        "Your company",
        listOf(StudioObject("desk-1", FurnitureKind.DESK, 47.0, 45.0, 72.0, 38.0)),
    )

    @Test fun validSceneRoundTripsWithFixedRoom() {
        assertEquals(sample, RoomSceneCodec.parse(RoomSceneCodec.encode(sample)))
    }

    @Test fun companySignCanBeEditedWithinFictionalDemo() {
        val edited = sample.copy(companyName = "A fictional team")
        assertEquals(edited, RoomSceneCodec.parse(RoomSceneCodec.encode(edited)))
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.validate(sample.copy(companyName = " padded ")) }
    }

    @Test fun importedRoomIdentityAndOutlineCannotBeEdited() {
        val json = RoomSceneCodec.encode(sample)
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.parse(json.replace("studio-demo", "another-room")) }
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.parse(json.replace("320.0", "321.0")) }
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.parse(json.replace("[24.0,0.0]", "[25.0,0.0]")) }
    }

    @Test fun completeFootprintMustFitChamferedCorner() {
        val crossing = sample.copy(objects = listOf(StudioObject("desk-1", FurnitureKind.DESK, 1.0, 1.0, 40.0, 40.0)))
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.validate(crossing) }
    }

    @Test fun duplicateIdsUnknownKindsAndExtraFieldsAreRejected() {
        val duplicate = sample.copy(objects = sample.objects + sample.objects.first().copy(kind = FurnitureKind.RUG))
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.validate(duplicate) }
        val json = RoomSceneCodec.encode(sample)
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.parse(json.replace("\"desk\"", "\"lamp\"")) }
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.parse(json.replace("\"objects\":", "\"untrusted\":true,\"objects\":")) }
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.parse(json.replace("\"x\":47.0", "\"extra\":1,\"x\":47.0")) }
    }

    @Test fun nonFiniteHugeAndUnsupportedVersionsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            RoomSceneCodec.validate(sample.copy(objects = listOf(sample.objects.first().copy(x = Double.NaN))))
        }
        assertFailsWith<IllegalArgumentException> {
            RoomSceneCodec.validate(sample.copy(objects = listOf(sample.objects.first().copy(width = 1e100))))
        }
        val json = RoomSceneCodec.encode(sample)
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.parse(json.replace("\"schemaVersion\":1", "\"schemaVersion\":2")) }
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.parse(json.replace("\"x\":47.0", "\"x\":1e999")) }
    }

    @Test fun objectCountIsBounded() {
        val many = sample.copy(objects = (1..25).map {
            StudioObject("desk-$it", FurnitureKind.DESK, 47.0, 45.0, 16.0, 16.0)
        })
        RoomSceneCodec.validate(many.copy(objects = many.objects.take(24)))
        assertFailsWith<IllegalArgumentException> { RoomSceneCodec.validate(many) }
    }
}
