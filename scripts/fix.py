import os
import glob
import re

src_dir = r"c:\Users\Mei\Desktop\Project\BookStore\src\main\java\bookstore"
files = glob.glob(os.path.join(src_dir, '**', '*.java'), recursive=True)

# Replacements for FQN found in string literals or code
fqn_replacements = {
    r'\bbookstore\.repository\.UserCouponRepository\b': r'bookstore.coupon.UserCouponRepository',
    r'\bbookstore\.bean\.UserCouponBean\b': r'bookstore.coupon.UserCouponBean',
    r'\bbookstore\.dto\.BookSalesDTO\b': r'bookstore.book.BookSalesDTO',
    r'\bbookstore\.dto\.MonthlySalesDTO\b': r'bookstore.book.MonthlySalesDTO',
    r'\bbookstore\.dto\.SalesOverviewDTO\b': r'bookstore.book.SalesOverviewDTO',
    r'\bbookstore\.bean\.OrderItem\b': r'bookstore.order.OrderItem',
    r'\bbookstore\.util\.JwtInterceptor\b': r'bookstore.common.JwtInterceptor',
    r'\bbookstore\.bean\.UserBean\b': r'bookstore.user.UserBean',
    r'\bbookstore\.repository\.ClubRegistrationsRepository\b': r'bookstore.bookclub.ClubRegistrationsRepository'
}

# Missing imports mapped to filename substring
missing_imports = {
    'AiService.java': ['import bookstore.book.BookDtoSimple;'],
    'BookClubService.java': ['import bookstore.bookclub.ClubRegistrationsRepository;'],
    'WebConfig.java': ['import bookstore.common.JwtInterceptor;'],
    'CouponController.java': ['import bookstore.coupon.UserCouponBean;']
}

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
        
    original = content
        
    for old, new in fqn_replacements.items():
        content = re.sub(old, new, content)
        
    filename = os.path.basename(f)
    if filename in missing_imports:
        for imp in missing_imports[filename]:
            if imp not in content:
                content = re.sub(r'^(package\s+[\w\.]+;)', rf'\1\n{imp}', content, count=1, flags=re.MULTILINE)
                
    if content != original:
        with open(f, 'w', encoding='utf-8') as file:
            file.write(content)
            
print("FQN Fix applied.")
