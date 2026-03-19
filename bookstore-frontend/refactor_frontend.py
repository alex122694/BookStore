import os
import shutil
import re

frontend_dir = r"c:\Users\Mei\Desktop\Project\BookStore\bookstore-frontend"
src_dir = os.path.join(frontend_dir, "src")

# Target directories
modules_dir = os.path.join(src_dir, "modules")
common_dir = os.path.join(src_dir, "common")

def ensure_dir(d):
    if not os.path.exists(d):
        os.makedirs(d)

# Mapping of module names back to paths
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

# The rename/move mapping
# Old relative path (from src/) -> New relative path (from src/)
moves = {}

def add_move(old, new):
    old_full = os.path.normpath(os.path.join(src_dir, old))
    new_full = os.path.normpath(os.path.join(src_dir, new))
    if os.path.exists(old_full):
        moves[old_full] = new_full

# 1. Move APIs -> modules/{module}/api.js
api_dir = os.path.join(src_dir, "api")
if os.path.exists(api_dir):
    for f in os.listdir(api_dir):
        if f in api_mapping:
            add_move(f"api/{f}", f"modules/{api_mapping[f]}")
        else:
            # base api interceptors
            add_move(f"api/{f}", f"common/api/{f}")

# 2. Move Views -> modules/{module}/views/...
views_dir = os.path.join(src_dir, "views")
if os.path.exists(views_dir):
    # Walk through views and assign them
    for root, dirs, files in os.walk(views_dir):
        rel_path = os.path.relpath(root, views_dir).replace('\\', '/')
        if rel_path == '.': continue
        
        assigned_module = None
        for old_prefix, new_prefix in module_mapping.items():
            if rel_path == old_prefix or rel_path.startswith(old_prefix + '/'):
                sub_path = rel_path[len(old_prefix):].strip('/')
                if sub_path:
                    dest = f"modules/{new_prefix}/{sub_path}"
                else:
                    dest = f"modules/{new_prefix}"
                
                # move files in this exact directory
                for f in files:
                    add_move(f"views/{rel_path}/{f}", f"{dest}/{f}")
                assigned_module = True
                break
        
        if not assigned_module and "Layout" not in rel_path and rel_path != "public":
            # Just move it to common/views / public etc if needed? Actually we'll keep Layout in src/views/Layout for now, 
            # Or move Layout to common/components/Layout? Let's leave unmapped views in src/views (e.g. Layout, admin/Home, admin/Login)
            pass

# Additionally handle specific unmapped files like Home.vue, Login.vue
add_move("views/admin/Home.vue", "views/admin/Home.vue") # keep
add_move("views/admin/LoginView.vue", "views/admin/LoginView.vue") # keep
add_move("views/public/HomePage.vue", "views/public/HomePage.vue") # keep
add_move("views/public/AboutUs.vue", "views/public/AboutUs.vue") # keep

# 3. Move components -> common/components
components_dir = os.path.join(src_dir, "components")
if os.path.exists(components_dir):
    for item in os.listdir(components_dir):
        add_move(f"components/{item}", f"common/components/{item}")

# 4. Move composables -> common/hooks
composables_dir = os.path.join(src_dir, "composables")
if os.path.exists(composables_dir):
    for item in os.listdir(composables_dir):
        add_move(f"composables/{item}", f"common/hooks/{item}")

# 5. Move utils -> common/utils
utils_dir = os.path.join(src_dir, "utils")
if os.path.exists(utils_dir):
    for item in os.listdir(utils_dir):
        add_move(f"utils/{item}", f"common/utils/{item}")

# 6. Move stores -> modules/{module}/store or common/stores
store_mapping = {
    'cartStore.js': 'cart/store/cartStore.js',
    'reportStore.js': 'review/store/reportStore.js',
    'userStore.js': 'user/store/userStore.js',
}
stores_dir = os.path.join(src_dir, "stores")
if os.path.exists(stores_dir):
    for item in os.listdir(stores_dir):
        if item in store_mapping:
            add_move(f"stores/{item}", f"modules/{store_mapping[item]}")
        else:
            add_move(f"stores/{item}", f"common/stores/{item}")

# DO THE MOVES
for old_path, new_path in moves.items():
    if not os.path.exists(old_path): continue
    ensure_dir(os.path.dirname(new_path))
    shutil.move(old_path, new_path)

# Try removing empty dirs
for root, dirs, files in os.walk(src_dir, topdown=False):
    for name in dirs:
        try:
            os.rmdir(os.path.join(root, name))
        except OSError:
            pass

# UPDATE IMPORTS in all files
def resolve_import_path(old_import):
    # Old import could be '@/views/admin/books/BooksHome.vue'
    # Or '@/api/bookService'
    # Or '@/components/ActionPageButton.vue'
    # We want to map it to the new path from src/
    
    # Clean up the import literal
    stripped = old_import.strip("'\"")
    if not stripped.startswith('@/'):
        # Relative imports are harder to map automatically if we don't know the exact current file path,
        # but in this codebase most imports are '@/' aliases. We'll handle '@/' primarily.
        return old_import
        
    rel_import = stripped[2:] # -> e.g. views/admin/books/BooksHome.vue
    
    # Let's search if rel_import matches one of our old mappings
    # Since imports might omit .vue or .js, we check exact prefixes
    
    # Component mappings
    if rel_import.startswith('components/'):
        return f"'{stripped.replace('components/', 'common/components/')}'"
    if rel_import.startswith('composables/'):
        return f"'{stripped.replace('composables/', 'common/hooks/')}'"
    if rel_import.startswith('utils/'):
        return f"'{stripped.replace('utils/', 'common/utils/')}'"
        
    # API mappings
    if rel_import.startswith('api/'):
        api_name = rel_import[4:]
        if not api_name.endswith('.js'):
            api_name += '.js'
        if api_name in api_mapping:
            new_api = api_mapping[api_name].replace('.js', '')
            return f"'@/modules/{new_api}'"
        else:
            return f"'@/common/api/{rel_import[4:]}'"
            
    # Views mappings
    if rel_import.startswith('views/'):
        for old_prefix, new_prefix in module_mapping.items():
            if rel_import.startswith('views/' + old_prefix):
                sub_path = rel_import[len('views/' + old_prefix):]
                return f"'@/modules/{new_prefix}{sub_path}'"

    # Store mappings
    if rel_import.startswith('stores/'):
        store_name = rel_import.replace('stores/', '')
        if not store_name.endswith('.js'): store_name += '.js'
        if store_name in store_mapping:
            path = store_mapping[store_name].replace('.js', '')
            return f"'@/modules/{path}'"
        else:
            return f"'@/common/stores/{rel_import[7:]}'"
                
    return old_import

def update_file_imports(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Regex for import xxx from '...'
    # also matches import('...') inside router
    def replacer(m):
        full_match = m.group(0)
        import_path = m.group(1)
        new_path = resolve_import_path(import_path)
        if new_path != import_path:
            return full_match.replace(import_path, new_path)
        return full_match

    # For standard imports: from '@/...'
    updated = re.sub(r'from\s+([\'"]@/[^\'"]+[\'"])', replacer, content)
    # For dynamic imports: import('@/...')
    updated = re.sub(r'import\s*\(\s*([\'"]@/[^\'"]+[\'"])\s*\)', replacer, updated)

    # Some imports are relative: from '../views/...', we need to be careful.
    # We will try to replace relative import paths that point to a view, component, or API.
    # Since the structure changed completely, relative imports to components or API will break.
    # Let's convert known relative imports to absolute `@/` temporarily, then resolve.
    # E.g. '../views/admin/LoginView.vue' -> '@/views/admin/LoginView.vue'
    
    def relative_replacer(m):
        # This is very specific to router/index.js where `../views/` is used
        relative_path = m.group(1) # e.g. '../views/admin/books/BooksHome.vue'
        if relative_path.startswith('../views/'):
            abs_path = "'@/" + relative_path[3:]
            return resolve_import_path(abs_path)
        elif relative_path.startswith('../../api/'):
            abs_path = "'@/" + relative_path[6:]
            return resolve_import_path(abs_path)
        elif relative_path.startswith('../api/'):
            abs_path = "'@/" + relative_path[3:]
            return resolve_import_path(abs_path)
        return m.group(1)
        
    updated = re.sub(r'import\s*\(\s*([\'"]\.\./[^\'"]+[\'"])\s*\)', lambda m: f"import({relative_replacer(m)})", updated)
    updated = re.sub(r'from\s+([\'"]\.\./[^\'"]+[\'"])', lambda m: f"from {relative_replacer(m)}", updated)
    # also handle ../../
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

print(f"Moved mapped files. Updated imports in {updated_count} files.")
