"""Synthetic topology checks for the offline decorative partition mask."""
import unittest

import cv2
import numpy as np

from scripts.style_plan_partitions import grow_partition_mask, mask_contours, rasterize_mask_contours


def hole_count(mask: np.ndarray) -> int:
    padded = np.pad((mask > 0).astype(np.uint8), 1)
    count, labels = cv2.connectedComponents((padded == 0).astype(np.uint8), connectivity=4)
    outside = labels[0, 0]
    return sum(np.any(labels == i) for i in range(1, count) if i != outside)


class StyledPartitionTests(unittest.TestCase):
    def test_disconnected_segments_stay_disconnected_across_small_gap(self):
        source = np.zeros((32, 48), np.uint8)
        source[15, 5:19] = 255
        source[15, 22:38] = 255
        styled, _ = grow_partition_mask(source)
        self.assertEqual(cv2.connectedComponents((styled > 0).astype(np.uint8), connectivity=8)[0] - 1, 2)
        self.assertFalse(np.any(styled[15, 19:22]))

    def test_gap_guard_keeps_one_two_and_five_pixel_openings(self):
        for gap in (1, 2, 5):
            with self.subTest(gap=gap):
                source = np.zeros((28, 48), np.uint8)
                source[14, 4:20] = 255
                source[14, 20 + gap:42] = 255
                styled, metrics = grow_partition_mask(source)
                self.assertTrue(np.all(styled[source > 0] > 0))
                self.assertFalse(np.any(styled[14, 20:20 + gap]))
                self.assertEqual(metrics["raw_components"], metrics["decorative_components"])
                self.assertEqual(metrics["raw_holes"], metrics["decorative_holes"])

    def test_hairpin_corridor_and_enclosed_opening_remain_open(self):
        source = np.zeros((40, 40), np.uint8)
        # C-shaped return with a three-pixel throat.
        source[8:32, 8] = source[8:32, 20] = 255
        source[8, 8:21] = source[31, 8:21] = 255
        source[8:32, 28] = 255
        source[8, 20:29] = source[31, 20:29] = 255
        styled, _ = grow_partition_mask(source)
        self.assertGreater(hole_count(styled), 0)
        self.assertFalse(np.all(styled[19:21, 21:28]))

    def test_stairs_are_not_thickened_and_raw_partition_ink_is_contained(self):
        source = np.zeros((36, 48), np.uint8)
        source[10:26, 8] = 255
        source[18, 20:35] = 255
        stairs = np.zeros_like(source)
        stairs[16:21, 16:21] = 255
        styled, metrics = grow_partition_mask(source, stairs)
        self.assertTrue(np.all(styled[source > 0] > 0))
        self.assertFalse(np.any(styled[stairs > 0] & ~(source[stairs > 0] > 0)))
        self.assertLessEqual(metrics["growth_pixels"], int(np.count_nonzero(styled)))

    def test_tiny_raw_speck_is_kept_without_growth(self):
        source = np.zeros((24, 24), np.uint8)
        source[5, 5:8] = 255
        source[15, 10:22] = 255
        styled, _ = grow_partition_mask(source)
        self.assertTrue(np.all(styled[source > 0] > 0))
        self.assertEqual(int(styled[4:7, 4:9].sum()), int(source[4:7, 4:9].sum()))

    def test_contour_export_preserves_holes_and_one_pixel_specks(self):
        source = np.zeros((48, 48), np.uint8)
        source[8:36, 8:36] = 255
        source[13:31, 13:31] = 0
        source[3, 3] = 255
        contours = mask_contours(source, 0, 0)
        rebuilt = rasterize_mask_contours(contours, source.shape, 0, 0)
        np.testing.assert_array_equal(rebuilt, source)
        self.assertEqual(hole_count(rebuilt), 1)


if __name__ == "__main__":
    unittest.main()
