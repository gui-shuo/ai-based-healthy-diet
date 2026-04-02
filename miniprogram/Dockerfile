FROM nginx:1.25-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY dist/build/h5/ /usr/share/nginx/html/

EXPOSE 80
