import os
import re
import glob
import numpy as np

def _get_val(line):
	return int(re.compile(r'. (?P<var>\d+)\n').search(line).group('var'))

def read_input(filename):
	text_io = open(filename, 'r')
	P = _get_val(text_io.readline())
	D = _get_val(text_io.readline())
	T = _get_val(text_io.readline())
	S = _get_val(text_io.readline())
	H = _get_val(text_io.readline())

	# hd
	text_io.readline()
	hd = np.array([int(text_io.readline()) for _ in range(D)])

	# apd
	text_io.readline()
	apd = np.array([list(map(int, text_io.readline().split())) for _ in range(P)])

	# rpt
	text_io.readline()
	rpt = np.array([list(map(int, text_io.readline().split())) for _ in range(P)])

	return P, D, T, S, H, hd, apd, rpt

def instance_names(folder='instances'):
	exp = os.path.join(folder, '*.pap')
	names = [path.split(os.path.sep)[-1] for path in glob.glob(exp)]
	def cmp(name):
		match = re.compile(r'P(?P<P>\d+)D(?P<D>\d+)S(?P<S>\d+)').search(name)
		return int(match.group('P')) + int(match.group('D')) + int(match.group('S'))
	return sorted(names, key=cmp)

if __name__ == "__main__":
	for instance in instance_names():
		print(read_input(f'instances/{instance}'))