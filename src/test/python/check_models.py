"""Fail on the coplanar overlapping faces responsible for machine-model flicker."""
from pathlib import Path
from itertools import combinations
import json

root = Path(__file__).resolve().parents[3] / 'src/main/resources/assets/farmhand/models'
faces = {'west': (0, 0), 'east': (0, 1), 'down': (1, 0), 'up': (1, 1), 'north': (2, 0), 'south': (2, 1)}
count = 0
for file in root.rglob('*.json'):
    elements = json.loads(file.read_text()).get('elements', [])
    for a, b in combinations(elements, 2):
        for name, (axis, high) in faces.items():
            if name not in a['faces'] or name not in b['faces']:
                continue
            boundary = 'to' if high else 'from'
            if a[boundary][axis] != b[boundary][axis]:
                continue
            other = [i for i in range(3) if i != axis]
            overlap = all(min(a['to'][i], b['to'][i]) > max(a['from'][i], b['from'][i]) for i in other)
            assert not overlap, f'{file.name}: {a.get("name")} overlaps {b.get("name")} on {name}'
    for element in elements:
        assert all(element['from'][i] < element['to'][i] for i in range(3)), f'{file}: degenerate cube'
        for face in element['faces'].values():
            assert all(0 <= uv <= 16 for uv in face.get('uv', [])), f'{file}: UV outside atlas'
    count += bool(elements)
print(f'Passed geometry/UV checks for {count} detailed block and item models.')

lamp_on = json.loads((root / 'block/growth_lamp.json').read_text())
lamp_off = json.loads((root / 'block/growth_lamp_off.json').read_text())
core_on = next(e for e in lamp_on['elements'] if e.get('name') == 'jade_core')
core_off = next(e for e in lamp_off['elements'] if e.get('name') == 'jade_core')
assert core_on.get('light_emission') == 15 and not core_on.get('shade', True), 'active jade core must glow'
assert not core_off.get('light_emission', 0) and core_off.get('shade', True), 'off jade core must not glow'
assert all(f['texture'] == '#unlit' for f in core_off['faces'].values()), 'off core needs the dim texture'
for name in ('chicken_coop', 'animal_feeder'):
    model = json.loads((root / f'block/{name}.json').read_text())
    assert not any(e.get('name') in ('egg_left', 'egg_right', 'dry_feed') for e in model['elements']), 'no fake inventory contents'
print('Passed lamp emissive/off-state and real-inventory model checks.')
