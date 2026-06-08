useEffect(() => {
    const interval = setInterval(fetchData, 60000);
    return () => clearInterval(interval);
}, []);