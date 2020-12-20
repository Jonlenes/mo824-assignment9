install:
	pip install -r src/requirements.txt

instance_check:
	python src/read_instance.py

run:
	python src/optimaze.py
