const initialState = {
    authUser: null,
    categories: [],
    products: [],
    cart:null,
    page: 'home',
};

const AppContext = React.createContext(null);

function reducer(state, action) {
    switch (action.type) {
        case 'authenticate':
            window.localStorage.setItem("auth-user", JSON.stringify(action.payload));
            return { ...state, authUser: action.payload };

        case 'categories':
            return { ...state, categories: action.payload };

        case 'cart':
            return { ...state, cart: action.payload };

        case 'products':
            return { ...state, products: action.payload };

        case 'logout':
            window.localStorage.removeItem("auth-user");
            return { ...state, authUser: null, cart: null };

        case 'search':
            return {
                ...state,
                page: 'search',
                searchQuery: action.payload.query, // сохраняем строку поиска
                searchResults: action.payload.results || [] // сохраняем результаты поиска
            };

        case 'navigate': {
            window.location.hash = action.payload;
            const [path, queryParams] = action.payload.split('?');
            const searchParams = new URLSearchParams(queryParams);

            return {
                ...state,
                page: path,
                searchQuery: searchParams.get('query') || '',
            };
        }



        default:
            return state;
    }
}

function App({contextPath, homePath}) {
    const [state, dispatch] = React.useReducer( reducer, initialState );
    const loadCategories = React.useCallback( () => {
        fetch(`${contextPath}/shop/category`)
            .then(r => r.json())
            .then(j => dispatch({type: 'categories', payload: j.data}));
    });
    const checkHash = React.useCallback( () => {
        let hash = window.location.hash;
        let path = ( hash.length > 1 ) ? hash.substring(1) : "home";
        dispatch( { type: "navigate", payload: path } );
    } ) ;

    const refreshCart = React.useCallback( () => {
        request('/shop/cart').then(cart => dispatch({type: "cart", payload: cart}))
            //.catch(console.error);
    });

    const request = React.useCallback( (url, params) => new Promise( (resolve, reject) => {
        if( url.startsWith('/') ) {
            url = contextPath + url;
        }
        if(state.authUser && state.authUser.token && state.authUser.token.tokenId) {
            if(typeof params === "undefined")
            {
                params = {};
            }
            if(typeof params.headers === "undefined")
            {
                params.headers = {};
            }
            if(typeof params.headers.Authorization === "undefined")
            {
                params.headers.Authorization = "Bearer " + state.authUser.token.tokenId;
            }
        }
        fetch( url, params )
            .then(r => r.json())
            .then(j => {
                if (j.status.isSuccessful) { resolve( j.data ); }
                else { reject( j.data ); }
            });
    } ) );
    React.useEffect( () => {
        let authUser = window.localStorage.getItem( "auth-user" );
        if( authUser ) {
            authUser = JSON.parse( authUser );
            let token = authUser.token;
            if( token ) {
                let exp = new Date(token.exp);
                if( exp < new Date() ) {
                    dispatch({type: 'logout'});
                }
                else {
                    dispatch({type: 'authenticate', payload: authUser});
                }
            }
        }
        checkHash();
        window.addEventListener('hashchange', checkHash);
        loadCategories();

        return () => {
            window.removeEventListener('hashchange', checkHash);
        };
    }, [] );

    React.useEffect(()=> {
        refreshCart();

    }, [state.authUser]);

    return <AppContext.Provider value={{state, dispatch, contextPath, loadCategories, request, refreshCart}}>
        <header>

            <nav
                className="navbar navbar-expand-lg"
                style={{backgroundColor: "#221F1F"}}
            >
                <div className="container-fluid">
                    <a
                        className="navbar-brand d-flex align-items-center"
                        onClick={() => dispatch({type: "navigate", payload: "home"})}
                        style={{
                            color: "white",
                            fontSize: "1.5rem",
                            fontWeight: "bold",
                            cursor: "pointer",
                        }}
                    >
            <span
                className="circle-logo rounded-circle me-2"
                style={{
                    backgroundColor: "#05BC52",
                    width: "48px",
                    height: "48px",
                    display: "inline-block",
                }}
            ></span>
                        ElectroNest
                    </a>

                    <button
                        className="navbar-toggler"
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#navbarSupportedContent"
                        aria-controls="navbarSupportedContent"
                        aria-expanded="false"
                        aria-label="Toggle navigation"
                    >
                        <span className="navbar-toggler-icon"></span>
                    </button>
                    <div className="collapse navbar-collapse" id="navbarSupportedContent">
                        <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                            <li className="nav-item">
                                <a
                                    className="nav-link"
                                    onClick={() => dispatch({type: "navigate", payload: "home"})}
                                    style={{color: "white", cursor: "pointer"}}
                                >
                                    Home
                                </a>
                            </li>
                            <li className="nav-item">
                                <a
                                    className="nav-link"
                                    onClick={() => dispatch({type: "navigate", payload: "cart"})}
                                    style={{color: "white", cursor: "pointer"}}
                                >
                                    Cart
                                    <span className="cart-widget-quantity">
                    {state.cart &&
                    state.cart.cartItems &&
                    state.cart.cartItems.length > 0
                        ? state.cart.cartItems.reduce(
                            (s, c) => s + c.quantity,
                            0
                        )
                        : 0}
                  </span>
                                </a>
                            </li>
                        </ul>
                        <form
                            className="d-flex nav-search"
                            role="search"
                            onSubmit={async (e) => {
                                e.preventDefault();
                                const searchInput = e.target.elements.search;
                                if (searchInput) {
                                    const searchQuery = searchInput.value.trim();
                                    if (searchQuery) {
                                        const newHash = `search?query=${encodeURIComponent(searchQuery)}`;
                                        dispatch({type: "navigate", payload: newHash});
                                        console.log("Navigating to search with query:", searchQuery);
                                    }
                                }
                            }}
                        >
                            <input
                                name="search"
                                className="form-control me-2"
                                type="search"
                                placeholder="Search"
                                aria-label="Search"
                            />
                            <button className="btn btn-outline-success" type="submit">
                                <i className="bi bi-search"></i>
                            </button>
                        </form>


                        {!state.authUser && (
                            <div>
                                <button
                                    type="button"
                                    className="btn btn-outline-secondary"
                                    data-bs-toggle="modal"
                                    data-bs-target="#authModal"
                                >
                                    <i className="bi bi-box-arrow-in-right"></i>
                                </button>
                                <button
                                    type="button"
                                    className="btn btn-outline-secondary"
                                    onClick={() =>
                                        dispatch({type: "navigate", payload: "signup"})
                                    }
                                >
                                    <i className="bi bi-person-add"></i>
                                </button>
                            </div>
                        )}

                        {state.authUser && (
                            <div>
                                <img
                                    src={"storage/" + (state.authUser.avatarUrl || "no-photo.png")}
                                    alt={state.authUser.userName}
                                    onClick={() =>
                                        dispatch({type: "navigate", payload: "profile"})
                                    }
                                    className="nav-avatar"
                                />

                                <button
                                    type="button"
                                    className="btn btn-outline-warning"
                                    onClick={() => dispatch({type: "logout"})}
                                >
                                    <i className="bi bi-box-arrow-right"></i>
                                </button>
                                {state.authUser.role.canCreate && (
                                    <button
                                        type="button"
                                        className="btn btn-outline-warning"
                                        onClick={() =>
                                            dispatch({type: "navigate", payload: "admin"})
                                        }
                                    >
                                        <i className="bi bi-speedometer2"></i>
                                    </button>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </nav>
        </header>
        <main className="container">
            {console.log("Current page:", state.page)}
            {state.page === 'admin' && <Admin/>}
            {state.page === 'cart' && <Cart/>}
            {state.page === 'home' && <Home/>}
            {state.page === 'profile' && <Profile/>}
            {state.page === 'search' && <SearchPage/>}
            {state.page === 'signup' && <Signup/>}
            {state.page.startsWith('category/') && <Category id={state.page.substring(9)}/>}
            {state.page.startsWith('product/') && <Product id={state.page.substring(8)}/>}
        </main>
        <div className="spacer"></div>

        <AuthModal/>

        <footer className="px-3 py-2 text-white" style={{backgroundColor: '#05BC52'}}>
            &copy; 2025, ElectroNest
        </footer>
    </AppContext.Provider>;
}

function Profile() {
    const {state, dispatch, request, refreshCart} = React.useContext(AppContext);
    const [carts, setCarts] = React.useState(null)
    React.useEffect(() => {
        if (!state.authUser) {
            dispatch({type: 'navigate', payload: 'home'});
        }
    }, [state.authUser]);

    const loadCarts = React.useCallback(() => {
        request('/shop/profile').then(cartsArr => {
            setCarts(cartsArr.sort((a, b) => {
                    a = new Date(a.createDt).getTime();
                    b = new Date(b.createDt).getTime();
                    return a < b ? -1 : (a > b ? 1 : 0);
                }
            ));
        }).catch(console.error);
    })

    React.useEffect(() => {
        loadCarts();
    }, []);

    const repeatCart = React.useCallback((cart) => {
        let msg;
        if (state.cart != null && state.cart.cartItems.length > 0) {
            msg = `Додати до наявного кошику всі позиції цього кошику?`
        } else {
            msg = 'Створити замовлення на базі цього кошику';
        }
        if (!confirm(msg)) {
            return;
        }
        request("/shop/cart", {
            method: "PATCH",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(cart.cartItems)
        }).then(absents => {
            refreshCart()
            loadCarts()
            if (absents.length > 0) {
                alert("Не всі товари зх кошику є в наявності, зокрема: " +
                    absents.map(ci => `${ci.name} -- ${ci.quantity} шт`)
                        .join('\n'));
            }
        }).catch(console.error);
    })

    return <div>
        <h1>Кабінет користувача</h1>

        {state.authUser ?
            <div className="row">
                <div className="col col-4">
                    <h2>Персональні дані</h2>
                    <p> Ім'я: {state.authUser.userName || 'Не зазначене'}  </p>
                    <p> Телефон: {state.authUser.phone || 'Не зазначене'}  </p>
                    <p> E-mail: {state.authUser.email || 'Не зазначене'}  </p>
                    <p> Дата народження: {state.authUser.birthdate || 'Не зазначене'}  </p>
                    <p>

                        Аватар: {state.authUser.avatarUrl
                        ? <img src={"storage/" + state.authUser.avatarUrl} alt="Avatar" style={{width: '90%'}}/>
                        : 'Не зазначен'}
                    </p>

                </div>
                <div className="col col-8">
                    <h2>Історія покупок</h2>
                    {carts === null && <i>Дані завантажуються ...</i>}
                    {carts !== null && carts.length === 0 && <i>Ви ще не здійснювали покупок</i>}
                    {carts !== null && carts.length > 0 && <div>
                        {carts.map(cart => <div className="row" key={cart.id}>
                            <div className="col">
                                <a className="btn btn-primary" data-bs-toggle="collapse" href={"#" + cart.id}
                                   role="button" aria-expanded="false" aria-controls="collapseExample">
                                    {new Date(cart.createDt).toDateString()}
                                </a>
                            </div>
                            <div className="col">{
                                cart.closeDt
                                    ? new Date(cart.closeDt).toDateString()
                                    : 'не закритий'
                            }</div>
                            <div className="col">
                                {cart.cartItems.reduce((prev, item) => prev + item.quantity, 0)}
                            </div>
                            <div className="col">
                                {/* Сумма с учетом скидки для всего заказа */}
                                {cart.cartItems.reduce((prev, item) => {
                                    // Применяем скидку, если она есть
                                    const discountedPrice = item.product.discount !== 0
                                        ? item.product.price * (1 - item.product.discount / 100)
                                        : item.product.price;
                                    return prev + discountedPrice * item.quantity;
                                }, 0.0).toFixed(2)}
                            </div>
                            <div className="col">
                                {cart.closeDt
                                    ? <button onClick={() => repeatCart(cart)}>Повторити</button>
                                    : <button onClick={() => dispatch({
                                        type: 'navigate',
                                        payload: 'cart'
                                    })}>Закрити</button>
                                }
                            </div>
                            <div className="collapse" id={cart.id}>
                                <div className="card card-body">
                                    <div className="row cart-row">
                                        <div className="col col-2">
                                            <br/>
                                        </div>
                                        <div className="col col-4">
                                            <h5>Назва</h5>
                                        </div>
                                        <div className="col col-3">
                                            <h5>Кількість</h5>
                                        </div>
                                        <div className="col col-3">
                                            <h5>Ціна</h5>
                                        </div>
                                    </div>
                                    {cart.cartItems.map(item => <div className="row cart-row" key={item.productId}>
                                        <div className="col col-2">
                                            <picture onClick={() => dispatch({
                                                type: 'navigate',
                                                payload: 'product/' + (item.product.slug || item.product.id)
                                            })}>
                                                <img src={"storage/" + item.product.imageUrl} alt="product"/>
                                            </picture>
                                        </div>
                                        <div className="col col-4">
                                            {item.product.name}
                                        </div>
                                        <div className="col col-3">
                                            {item.quantity}
                                        </div>
                                        <div className="col col-3">
                                            {/* Цена с учетом скидки */}
                                            {item.product.discount !== 0
                                                ? <span>
                                    <del>{item.product.price.toFixed(2)}</del>
                                    <span style={{color: 'red'}}>
                                        {(item.product.price * (1 - item.product.discount / 100)).toFixed(2)}
                                    </span>
                                  </span>
                                                : item.product.price.toFixed(2)
                                            }
                                        </div>
                                    </div>)}
                                </div>
                            </div>
                        </div>)}
                    </div>}
                </div>
                <div className="col col-4"></div>
            </div>
            : <div> Необхідно автентифікуватись </div>}
    </div>;
}

function SearchPage() {
    const { request } = React.useContext(AppContext);
    const query = new URLSearchParams(window.location.hash.split('?')[1]).get('query');
    const [products, setProducts] = React.useState([]); // Убедитесь, что переменная определена

    React.useEffect(() => {
        if (query) {
            // Выполнить запрос на сервер с параметром поиска
            request(`/shop/product?search=${encodeURIComponent(query)}`)
                .then((data) => {
                    console.log("Search Results:", data);
                    setProducts(data); // Сохраняем полученные товары в состоянии
                })
                .catch((error) => {
                    console.error("Error fetching search results:", error);
                });
        }
    }, [query, request]); // Выполняем эффект при изменении `query` или `request`

    return (
        <div>
            <h2>Search Results</h2>
            {query ? (
                <p>Results for: <strong>{query}</strong></p>
            ) : (
                <p>No search query provided.</p>
            )}
            <div className="product-list">
                {products.length > 0 ? (
                    products.map((product) => (
                        <ProductCard p={product} key={product.id} />
                    ))
                ) : (
                    <p>No products found.</p>
                )}
            </div>
        </div>
    );
}

function Admin() {
    const {state, dispatch, contextPath, loadCategories} = React.useContext(AppContext);
    React.useEffect(() => {
        if (!state.authUser || !state.authUser.role || !state.authUser.role.canCreate) {
            dispatch({type: 'navigate', payload: 'home'});
        }
    }, []);
    const categoryFormRef = React.useRef();
    const productFormRef = React.useRef();
    const onCategorySubmit = React.useCallback(e => {
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch(`${contextPath}/shop/category`, {
            method: "POST",
            body: formData
        }).then(r => r.json()).then(j => {
            if (j.status.isSuccessful) {
                alert("Категорія успішно створена");
                categoryFormRef.current.reset();
                loadCategories();
            } else {
                alert(j.data);
            }
        });
    });
    const onProductSubmit = React.useCallback(e => {
        e.preventDefault();
        const formData = new FormData(e.target);
        for (let [key, value] of formData.entries()) {
            console.log(`${key}: ${value}`);
        }

        fetch(`${contextPath}/shop/product`, {
            method: "POST",
            body: formData
        }).then(r => r.json()).then(j => {
            if (j.status.isSuccessful) {
                alert("Товар успішно створений");
                productFormRef.current.reset();
            } else {
                alert(j.data);
            }
        });
    });

    return <div>
        <h1>Панель адміністрування</h1>
        <hr/>
        <h2>Створення товарних категорій</h2>
        <form encType="multipart/form-data" method="POST"
              onSubmit={onCategorySubmit} ref={categoryFormRef}>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="name-addon"><i className="bi bi-info-square"></i></span>
                        <input type="text" className="form-control"
                               name="category-name" placeholder="Назва"
                               aria-label="Назва" aria-describedby="name-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                    <span className="input-group-text" id="description-addon"><i
                        className="bi bi-card-text"></i></span>
                        <input type="text" className="form-control"
                               name="category-description" placeholder="Опис"
                               aria-label="Опис" aria-describedby="description-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <label className="input-group-text" htmlFor="category-image"><i
                            className="bi bi-card-image"></i></label>
                        <input type="file" className="form-control" name="category-image" id="category-image"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="slug-addon"><i className="bi bi-link"></i></span>
                        <input type="text" className="form-control"
                               name="category-slug" placeholder="Slug"
                               aria-label="Slug" aria-describedby="slug-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">

                </div>
                <div className="col col-6">
                    <button type="submit" className="btn btn-outline-success">Створити</button>
                </div>
            </div>
        </form>
        <hr/>
        <h2>Створення товарів</h2>
        <form encType="multipart/form-data" method="POST"
              onSubmit={onProductSubmit} ref={productFormRef}>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="product-name-addon"><i className="bi bi-info-square"></i></span>
                        <input type="text" className="form-control"
                               name="product-name" placeholder="Назва"
                               aria-label="Назва" aria-describedby="product-name-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                    <span className="input-group-text" id="product-description-addon"><i
                        className="bi bi-card-text"></i></span>
                        <input type="text" className="form-control"
                               name="product-description" placeholder="Опис"
                               aria-label="Опис" aria-describedby="product-description-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <label className="input-group-text" htmlFor="product-image"><i
                            className="bi bi-card-image"></i></label>
                        <input type="file" className="form-control" name="product-image" id="product-image"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="product-slug-addon"><i className="bi bi-link"></i></span>
                        <input type="text" className="form-control"
                               name="product-slug" placeholder="Slug"
                               aria-label="Slug" aria-describedby="product-slug-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                    <span className="input-group-text" id="product-price-addon"><i
                        className="bi bi-cash"></i></span>
                        <input type="number" step="0.01" className="form-control"
                               name="product-price" placeholder="Ціна"
                               aria-label="Ціна" aria-describedby="product-price-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <select name="category-id" className="form-select" aria-label="Вибір категорії">
                        {state.categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                    <span className="input-group-text" id="product-quantity-addon"><i
                        className="bi bi-1-circle"></i></span>
                        <input type="number" step="1" className="form-control" defaultValue="1"
                               name="product-quantity" placeholder="Кількість"
                               aria-label="Кількість" aria-describedby="product-quantity-addon"/>
                    </div>

                </div>

                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="product-discount-addon"><i className="bi bi-percent"></i></span>
                        <input type="number" step="1" className="form-control"
                               name="discount" placeholder="Скидка (%)"
                               aria-label="Скидка" aria-describedby="product-discount-addon"/>
                    </div>
                </div>
            </div>

            <div className="row">
                <div className="col col-6">
                    <button type="submit" className="btn btn-outline-success">Створити</button>
                </div>
            </div>
        </form>
        <hr/>
    </div>;
}

function Signup() {
    const {contextPath} = React.useContext(AppContext);
    const formRef = React.useRef();
    const onFormSubmit = React.useCallback(e => {
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch(`${contextPath}/auth`, {
            method: "POST",
            body: formData
        }).then(r => r.json()).then(j => {
            if (j.status.isSuccessful) {
                alert("Ви успішно зареєстровані");
                formRef.current.reset();
            } else {
                alert(j.data);
            }
        });
    });
    return <div>
        <h1>Реєстрація нового користувача</h1>

        <form encType="multipart/form-data" method="POST"
              onSubmit={onFormSubmit} ref={formRef}>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="name-addon"><i className="bi bi-person-badge"></i></span>
                        <input type="text" className="form-control"
                               name="signup-name" placeholder="Ім'я"
                               aria-label="Ім'я" aria-describedby="name-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="birthdate-addon"><i className="bi bi-cake"></i></span>
                        <input type="date" className="form-control"
                               name="signup-birthdate" placeholder="Дата народження"
                               aria-label="Дата народження" aria-describedby="birthdate-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="phone-addon"><i className="bi bi-phone"></i></span>
                        <input type="text" className="form-control"
                               name="signup-phone" placeholder="Телефон"
                               aria-label="Телефон" aria-describedby="phone-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="email-addon"><i className="bi bi-envelope-at"></i></span>
                        <input type="text" className="form-control"
                               name="signup-email" placeholder="Ел. пошта"
                               aria-label="Ел. пошта" aria-describedby="email-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                    <span className="input-group-text" id="login-addon"><i
                        className="bi bi-box-arrow-in-right"></i></span>
                        <input type="text" className="form-control"
                               name="signup-login" placeholder="Логін"
                               aria-label="Логін" aria-describedby="login-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <label className="input-group-text" htmlFor="signup-avatar"><i className="bi bi-person-circle"></i></label>
                        <input type="file" className="form-control" name="signup-avatar" id="signup-avatar"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="password-addon"><i className="bi bi-lock"></i></span>
                        <input type="text" className="form-control"
                               name="signup-password" placeholder="Вигадайте пароль"
                               aria-label="Вигадайте пароль" aria-describedby="password-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="repeat-addon"><i className="bi bi-unlock"></i></span>
                        <input type="text" className="form-control"
                               name="signup-repeat" placeholder="Повторіть пароль"
                               aria-label="Повторіть пароль" aria-describedby="repeat-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <button type="submit" className="btn btn-outline-success">Реєстрація</button>
            </div>
        </form>
    </div>
}

function AuthModal() {
    const {contextPath, dispatch} = React.useContext(AppContext);
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const authModalRef = React.useRef();
    const authClick = React.useCallback(() => {
        console.log(login, password);
        fetch(`${contextPath}/auth`, {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + btoa(login + ':' + password)
            }
        }).then(r => r.json()).then(j => {
            console.log(j);
            if (j.status.isSuccessful) {
                // j.data - дані про користувача, токен та права (роль)
                // задача: зберегти ці дані і використовувати без повторної автентифікації
                // куди можна зберігати? а) state/context б) sessionStorage в) localStorage
                dispatch({type: 'authenticate', payload: j.data});
                bootstrap.Modal.getInstance(authModalRef.current).hide();
            } else {
                alert(j.data);
            }
        });
    });
    return <div className="modal fade" id="authModal" tabIndex="-1" ref={authModalRef}
                aria-labelledby="exampleModalLabel" aria-hidden="true">
        <div className="modal-dialog">
            <div className="modal-content">
                <div className="modal-header">
                    <h1 className="modal-title fs-5" id="exampleModalLabel">Вхід до системи</h1>
                    <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div className="modal-body">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="login-addon"><i
                            className="bi bi-person-fill-lock"></i></span>
                        <input type="text" className="form-control" placeholder="Логін" aria-label="Логін"
                               onChange={e => setLogin(e.target.value)}
                               aria-describedby="login-addon"/>
                    </div>
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="password-addon"><i className="bi bi-key-fill"></i></span>
                        <input type="password" className="form-control" placeholder="******" aria-label="Пароль"
                               onChange={e => setPassword(e.target.value)}
                               aria-describedby="password-addon"/>
                    </div>
                </div>
                <div className="modal-footer">
                    <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Скасувати</button>
                    <button type="button" className="btn btn-primary" onClick={authClick}>Вхід</button>
                </div>
            </div>
        </div>
    </div>;
}

function Cart() {
    const {state, dispatch, request, refreshCart} = React.useContext(AppContext);
    React.useEffect(()=>{
    }, [state.authUser])

    const incCartItem = React.useCallback ((item, delta) => {
        if(Number(item.quantity) + Number(delta) === 0)
        {
            if(!confirm("Видалити товар з кошику?"))
            {
                return;
            }
        }

        request(`/shop/cart?cart-id=${item.cartId}&product-id=${item.productId}&delta=${delta}`, {
            method: 'PUT',

        }).then(refreshCart).catch(alert);

    });

    const delCartItem = React.useCallback(item =>{
        if(!confirm("Скасувати весь кошик?"))
        {
            return;
        }
        request(`/shop/cart?cart-id=${item.cartId}&product-id=${item.productId}`, {
            method: 'DELETE',

        }).then(refreshCart).catch(alert);
    })

    const delCart = React.useCallback(() =>{
        if(!confirm("Скасувати весь кошик?"))
        {
            return;
        }
        request(`/shop/cart?cart-id=${state.cart.id}`, {
            method: 'DELETE',

        }).then(refreshCart).catch(alert);
    })

    const buyCart = React.useCallback(() => {
        const totalAmount = state.cart.cartItems.reduce((s, c) => {
            const discountedPrice = c.product.discount !== 0
                ? c.product.price * (1 - c.product.discount / 100)
                : c.product.price;
            return s + (discountedPrice * c.quantity);
        }, 0.0).toFixed(2);

        if (!confirm(`Підтверджуєте покупку на суму ${totalAmount} грн?`)) {
            return;
        }

        // Если пользователь подтвердил покупку, выполняем логику для оформления заказа
        // request(`/shop/cart?cart-id=${item.cartId}&product-id=${item.productId}`, {
        //     method: 'DELETE',
        // }).then(refreshCart).catch(alert);
    });


    return <div>
        <h2>Кошик</h2>

        {state.cart && state.cart.cartItems
            ? <React.Fragment>
                <div className="row cart-row">
                    <div className="col col-2"><br/></div>
                    <div className="col col-3"><h5>Назва</h5></div>
                    <div className="col col-3"><h5>Кількість</h5></div>
                    <div className="col col-2"><h5>Ціна</h5></div>
                    <div className="col col-2"><br/></div>
                </div>

                {state.cart.cartItems.map(item => {
                    // Рассчитываем цену товара с учетом скидки
                    const discountedPrice = item.product.discount !== 0
                        ? item.product.price * (1 - item.product.discount / 100)
                        : item.product.price;

                    return (
                        <div className="row cart-row" key={item.productId}>
                            <div className="col col-2">
                                <picture onClick={() => dispatch({
                                    type: 'navigate',
                                    payload: 'product/' + (item.product.slug || item.product.id)})}>
                                    <img src={"storage/" + item.product.imageUrl} alt="product"/>
                                </picture>
                            </div>
                            <div className="col col-3">
                                {item.product.name}
                            </div>
                            <div className="col col-3">
                                {item.quantity}
                            </div>
                            <div className="col col-2">
                                {item.product.discount !== 0 ? (
                                    <span>
                                    <h4 style={{ textDecoration: 'line-through', color: 'gray' }}>
                                        ₴ {item.product.price.toFixed(2)}
                                    </h4>
                                    <h4 style={{ color: 'red' }}>
                                        ₴ {discountedPrice.toFixed(2)}
                                    </h4>
                                </span>
                                ) : (
                                    <h4>₴ {item.product.price.toFixed(2)}</h4>
                                )}
                            </div>
                            <div className="col col-2">
                                <button onClick={() => incCartItem(item, -1)} className="btn btn-outline-warning">
                                    <i className="bi bi-bag-dash"></i>
                                </button>
                                <button onClick={() => incCartItem(item, 1)} className="btn btn-outline-success">
                                    <i className="bi bi-bag-plus"></i>
                                </button>
                                <button onClick={() => delCartItem(item)} className="btn btn-outline-danger">
                                    <i className="bi bi-bag-x"></i>
                                </button>
                            </div>
                        </div>
                    );
                })}

                {state.cart.cartItems.length > 0 && <div className="row">
                    <div className="col offset-4 col-1">Разом</div>
                    <div className="col col-3">
                        {state.cart.cartItems.reduce((s, c) => s + c.quantity, 0)}
                    </div>
                    <div className="col col-2">
                        {state.cart.cartItems.reduce((s, c) => {
                            const discountedPrice = c.product.discount !== 0
                                ? c.product.price * (1 - c.product.discount / 100)
                                : c.product.price;
                            return s + (discountedPrice * c.quantity);
                        }, 0).toFixed(2)}
                    </div>
                    <div className="col">
                        <button onClick={() => buyCart()} className="btn btn-success" title="Придбати">
                            <i className="bi bi-basket3"></i>
                        </button>
                        <button onClick={() => delCart()} className="btn btn-danger" title="Скасувати">
                            <i className="bi bi-trash3"></i>
                        </button>
                    </div>
                </div>}
            </React.Fragment>
            : <h3>Кошик порожній</h3>}

        <b onClick={() => dispatch({type: "navigate", payload: "home"})}>На Домашню</b>
    </div>;
}

function Home() {
    const {state, dispatch} = React.useContext(AppContext);
    return <div>
        <h2>Categories</h2>
        {state.categories.map(c => <div
            key={c.id} className="home-category"
            onClick={() => dispatch({type: 'navigate', payload: 'category/' + (c.slug || c.id)})}>
            <h3>{c.name}</h3>
            <picture>
                <img src={"storage/" + c.imageUrl} alt="category"/>
            </picture>
            <p>{c.description}</p>
        </div>)}
    </div>;
}

function Category({id}) {
    const {state, contextPath} = React.useContext(AppContext);
    const [products, setProducts] = React.useState([]);
    React.useEffect(() => {
        console.log(state.cart)
        fetch(`${contextPath}/shop/product?category=${id}`)
            .then(r => r.json())
            .then(j => {
                if (j.status.isSuccessful) {
                    setProducts(j.data);
                }
                else {
                    console.error(j.data);
                }
            });
    }, [id]);
    return <div>
        <h2>Category page: {id}</h2>
        {products.map(p => <ProductCard p = {p} key={p.id}/>)}
    </div>;
}

function Product({ id, contextPath }) {
    const { request, dispatch, refreshCart } = React.useContext(AppContext);
    const [product, setProduct] = React.useState({});
    const [reviews, setReviews] = React.useState([]);

    React.useEffect(() => {
        // Загружаем данные продукта
        request('/shop/product?id=' + id)
            .then(setProduct)
            .catch(console.error);
    }, [id]);



    const onReviewSubmit = React.useCallback(e => {
        e.preventDefault();
        const formData = new FormData(e.target);
        for (let [key, value] of formData.entries()) {
            console.log(`${key}: ${value}`);
        }
        formData.append("product_id", product.id);

        fetch("http://localhost:2060/Course/shop/review", {
            method: "POST",
            body: formData
        }).then(r => r.json()).then(j => {
            if (j.status.isSuccessful) {
                alert("Product успішно створений");
            } else {
                alert(j.data);
            }
        });
    });

    const cartClick = React.useCallback(e => {
        request('/shop/cart?product-id=' + product.id, {
            method: 'POST'
        }).then(refreshCart).catch(alert);
    });

    return (
        <div>
            <h2>Сторінка товару</h2>
            {product.id && (
                <div>
                    <div className="row">
                        <div className="col col-5">
                            <div className="product-page-left">
                                <picture>
                                    <img src={"storage/" + product.imageUrl} alt="product" />
                                </picture>
                            </div>
                        </div>
                        <div className="col col-7">
                            <h3>{product.name}</h3>
                            <p>{product.description}</p>

                            {/* Проверка скидки */}
                            <div>
                                {product.discount !== 0 ? (
                                    <React.Fragment>
                                        <h4
                                            style={{
                                                textDecoration: "line-through",
                                                color: "gray",
                                            }}
                                        >
                                            {product.price.toFixed(2)} грн
                                        </h4>
                                        <h4 style={{ color: "red" }}>
                                            {(
                                                product.price -
                                                product.price * (product.discount / 100)
                                            ).toFixed(2)}{" "}
                                            грн (-{product.discount}%)
                                        </h4>
                                        <p>
                                            Ваша економія:{" "}
                                            {(
                                                product.price *
                                                (product.discount / 100)
                                            ).toFixed(2)}{" "}
                                            грн
                                        </p>
                                    </React.Fragment>
                                ) : (
                                    <h4>{product.price.toFixed(2)} грн</h4>
                                )}
                            </div>

                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    cartClick();
                                }}
                            >
                                До кошику
                            </button>
                            <hr />
                            <h5>Вас також може зацікавити:</h5>
                            {product.similarProducts &&
                                product.similarProducts.map((p) => (
                                    <ProductCard p={p} isSmall={true} key={p.id} />
                                ))}
                        </div>
                    </div>

                    <hr />
                    <h3>Відгуки:</h3>
                    {reviews.length > 0 ? (
                        reviews.map(review => (
                            <div key={review.review_id}>
                                <h4>{review.review_name}</h4>
                                <p>{review.review_message}</p>
                            </div>
                        ))
                    ) : (
                        <p>Немає відгуків для цього товару</p>
                    )}

                    <hr />
                    <h3>Додати відгук:</h3>
                    <form encType="multipart/form-data" method="POST" onSubmit={onReviewSubmit}>
                        <div className="row">
                            <div className="col col-6">
                                <div className="input-group mb-3">
                                    <span className="input-group-text" id="review_name-addon">
                                        <i className="bi bi-info-square"></i>
                                    </span>
                                    <input
                                        type="text"
                                        className="form-control"
                                        name="review_name"
                                        placeholder="Name"
                                        aria-label="Name"
                                        aria-describedby="review_name-addon"
                                    />
                                </div>
                            </div>
                            <div className="col col-6">
                                <div className="input-group mb-3">
                                    <span className="input-group-text" id="review_message-addon">
                                        <i className="bi bi-card-text"></i>
                                    </span>
                                    <input
                                        type="text"
                                        className="form-control"
                                        name="review_message"
                                        placeholder="Review"
                                        aria-label="Review"
                                        aria-describedby="review_message-addon"
                                    />
                                </div>
                            </div>
                        </div>
                        <button type="submit" className="btn btn-outline-success">
                            Додати відгук
                        </button>
                    </form>
                </div>
            )}

            {!product.id && <div>Не знайдено</div>}
        </div>
    );
}

function ProductCard({p, isSmall}) {

    const {state, dispatch, request, refreshCart} = React.useContext(AppContext);


    const cartPost = React.useCallback( (e, product) => {
        e.stopPropagation();
        request('/shop/cart?product-id=' + product.id, {
            method: 'POST'
        }).then(refreshCart).catch(alert);
    });
    const cartPut = React.useCallback( (e, product) => {
        e.stopPropagation();
        request( `/shop/cart?cart-id=${state.cart.id}&product-id=${product.id}&delta=1`, {
            method: 'PUT'
        }).then(refreshCart).catch(alert);
    });

    const discountedPrice = p.discount !== 0 ? p.price - (p.price * (p.discount / 100)) : p.price;

    return (
        <div key={p.id} className={"product-card " + (isSmall ? "scale-75" : "")}
             onClick={() => dispatch({type: 'navigate', payload: 'product/' + (p.slug || p.id)})}>
            <picture>
                <img src={"storage/" + p.imageUrl} alt="product" />
            </picture>
            <h3>{p.name}</h3>
            <p>{p.description}</p>

            {/* Используем React.Fragment */}
            {p.discount !== 0 ? (
                <React.Fragment>
                    <h4 style={{ textDecoration: 'line-through', color: 'gray' }}>
                        ₴ {p.price.toFixed(2)}
                    </h4>
                    <h4 style={{ color: 'red' }}>
                        ₴ {discountedPrice.toFixed(2)}
                    </h4>
                    <p style={{ color: 'red' }}>
                        Скидка: {p.discount}%!
                    </p>
                </React.Fragment>
            ) : (
                <h4>₴ {p.price.toFixed(2)}</h4>
            )}

            {(state.cart && state.cart.cartItems && state.cart.cartItems.some(ci => ci.productId === p.id))
                ? <span className="cart-fab" onClick={(e) => cartPut(e, p)}>
                <i className="bi bi-bag-check"></i></span>
                : <span className="cart-fab" onClick={(e) => cartPost(e, p)}>
                <i className="bi bi-plus-circle"></i></span>
            }
        </div>
    );
}


const domRoot = document.getElementById("app-container");
const cp = domRoot.getAttribute("data-context-path");
const hp = domRoot.getAttribute("data-home-path");
// console.log(cp);
ReactDOM
    .createRoot(domRoot)
    .render(<App contextPath={cp} homePath={hp}/>);
