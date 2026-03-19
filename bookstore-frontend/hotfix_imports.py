import os
import re

frontend_dir = r"c:\Users\Mei\Desktop\Project\BookStore\bookstore-frontend"
src_dir = os.path.join(frontend_dir, "src")

module_mapping = {
    'admin/books': 'book/views/admin',
    'public/books': 'book/views/public',
    'admin/users': 'user/views/admin',
    'public/user': 'user/views/public',
    'admin/orders': 'order/views/admin',
    'public/orders': 'order/views/public',
    'public/cart': 'cart/views',
    'admin/coupons': 'coupon/views/admin',
    'admin/bookClubs': 'bookclub/views/admin',
    'public/club': 'bookclub/views/public',
    'admin/reviews': 'review/views/admin',
    'public/reviews': 'review/views/public',
    'admin/reports': 'review/views/admin', # review reports
    'admin/logs': 'stocklog/views/admin',
}

api_mapping = {
    'bookService.js': 'book/api.js',
    'bookClubService.js': 'bookclub/api.js',
    'couponService.js': 'coupon/api.js',
    'orderService.js': 'order/api.js',
    'reviewService.js': 'review/api.js',
    'stockLogService.js': 'stocklog/api.js'
}

store_mapping = {
    'cartStore.js': 'cart/store/cartStore.js',
    'reportStore.js': 'review/store/reportStore.js',
    'userStore.js': 'user/store/userStore.js',
}

def resolve_import_path(old_import):
    stripped = old_import.strip("'\"")
    if not stripped.startswith('@/'):
        return old_import
    rel_import = stripped[2:]
    quote = old_import[0]
    
    if rel_import.startswith('components/'):
        return f"{quote}@/common/components/{rel_import[11:]}{quote}"
    if rel_import.startswith('composables/'):
        return f"{quote}@/common/hooks/{rel_import[12:]}{quote}"
    if rel_import.startswith('utils/'):
        return f"{quote}@/common/utils/{rel_import[6:]}{quote}"
        
    if rel_import.startswith('api/'):
        api_name = rel_import[4:]
        if not api_name.endswith('.js'): api_name += '.js'
        if api_name in api_mapping:
            new_api = api_mapping[api_name].replace('.js', '')
            return f"{quote}@/modules/{new_api}{quote}"
        else:
            return f"{quote}@/common/api/{rel_import[4:]}{quote}"
            
    if rel_import.startswith('views/'):
        for old_prefix, new_prefix in module_mapping.items():
            if rel_import.startswith('views/' + old_prefix):
                sub_path = rel_import[len('views/' + old_prefix):]
                return f"{quote}@/modules/{new_prefix}{sub_path}{quote}"
                
    if rel_import.startswith('stores/'):
        store_name = rel_import.replace('stores/', '')
        if not store_name.endswith('.js'): store_name += '.js'
        if store_name in store_mapping:
            path = store_mapping[store_name].replace('.js', '')
            return f"{quote}@/modules/{path}{quote}"
        else:
            return f"{quote}@/common/stores/{rel_import[7:]}{quote}"
                
    return old_import

def relative_replacer(m):
    full_str = m.group(1)
    stripped = full_str.strip("'\"")
    quote = full_str[0]
    
    if stripped.startswith('../views/'):
        abs_path = f"{quote}@/{stripped[3:]}{quote}"
        return resolve_import_path(abs_path)
    elif stripped.startswith('../../api/'):
        abs_path = f"{quote}@/{stripped[6:]}{quote}"
        return resolve_import_path(abs_path)
    elif stripped.startswith('../api/'):
        abs_path = f"{quote}@/{stripped[3:]}{quote}"
        return resolve_import_path(abs_path)
    elif stripped.startswith('../../components/'):
        abs_path = f"{quote}@/{stripped[6:]}{quote}"
        return resolve_import_path(abs_path)
    elif stripped.startswith('../components/'):
        abs_path = f"{quote}@/{stripped[3:]}{quote}"
        return resolve_import_path(abs_path)
    return full_str

def update_file_imports(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    updated = re.sub(r'import\s*\(\s*([\'"]\.\./[^\'"]+[\'"])\s*\)', lambda m: f"import({relative_replacer(m)})", content)
    updated = re.sub(r'from\s+([\'"]\.\./[^\'"]+[\'"])', lambda m: f"from {relative_replacer(m)}", updated)
    updated = re.sub(r'from\s+([\'"]\.\./\.\./[^\'"]+[\'"])', lambda m: f"from {relative_replacer(m)}", updated)

    if updated != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(updated)
        return True
    return False

updated_count = 0
for root, dirs, files in os.walk(src_dir):
    for f in files:
        if f.endswith('.vue') or f.endswith('.js'):
            if update_file_imports(os.path.join(root, f)):
                updated_count += 1

print(f"Hotfixed relative imports in {updated_count} files.")
