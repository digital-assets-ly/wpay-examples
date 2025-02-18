From bitnami/laravel:latest

WORKDIR /app

COPY . .

# php artisan serve --port=8080

RUN composer install

EXPOSE 8080

CMD ["php", "artisan", "serve", "--port=8080", "--host=0.0.0.0"]
