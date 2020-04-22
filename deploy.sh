project=your-project
repository=cachereload
docker build . --tag gcr.io/$project/$repository
docker push gcr.io/$project/$repository
