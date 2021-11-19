import unittest
import util.misc as _


class MyTestCase(unittest.TestCase):
    def test_get_v(self):
        data = {
            'ladder': {'step1': {'step2': {'step3': 'finish'}}},
            'yaharr': ['zero', 'one', 'two', 'three'],
            'plain_str': 'Hello!'
        }
        self.assertEqual(
            {'step3': 'finish'},
            _.get_v(data, 'ladder.step1.step2')
        )
        self.assertEqual('Hello!', _.get_v(data, 'plain_str'))
        self.assertEqual('two', _.get_v(data, ['yaharr', '2']))

    def test_set_v(self):
        data = {'field': 'value'}
        _.set_v(data, ['extra_field', 0, 'hidden'], 'loot')
        self.assertEqual(
            {'field': 'value', 'extra_field': [{'hidden': 'loot'}]},
            data
        )

    def test_difference(self):
        arr = [2, 3, 4, 1, 5, 7]
        other = [7, 5, 3]

        self.assertEqual([2, 4, 1], _.difference(arr, other))


if __name__ == '__main__':
    unittest.main()
